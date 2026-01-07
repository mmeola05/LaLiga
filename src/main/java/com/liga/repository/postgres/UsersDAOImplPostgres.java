package com.liga.repository.postgres;

import com.liga.model.Alineacion;
import com.liga.model.TipoUsuario;
import com.liga.model.Usuario;
import com.liga.repository.dao.UsersDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsersDAOImplPostgres implements UsersDAO {

    private static final String URL = "jdbc:postgresql://localhost:5432/la_liga";
    private static final String USER = "admin";
    private static final String PASS = "admin";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    @Override
    public List<Usuario> findAll() {
        String sql = "SELECT id, tipo, email, password, saldo, equipo_id FROM usuarios";
        List<Usuario> result = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(mapUsuario(rs, conn));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener usuarios", e);
        }

        return result;
    }

    @Override
    public Optional<Usuario> findById(String id) {
        String sql = "SELECT id, tipo, email, password, saldo, equipo_id FROM usuarios WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUsuario(rs, conn));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar usuario por id: " + id, e);
        }

        return Optional.empty();
    }

    @Override
    public void save(Usuario usuario) {
        // uso de conexión propia para este save
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                saveUsingConnection(conn, usuario);
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar usuario", e);
        }
    }

    @Override
    public void saveAll(List<Usuario> usuarios) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                for (Usuario u : usuarios) {
                    saveUsingConnection(conn, u);
                }
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar lista de usuarios", e);
        }
    }

    @Override
    public void deleteById(String id) {
        String sql = "DELETE FROM usuarios WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);
            ps.executeUpdate();

            // borrar dependencias (alineaciones/plantilla)
            try (PreparedStatement p2 = conn.prepareStatement("DELETE FROM alineaciones WHERE usuario_id = ?")) {
                p2.setString(1, id);
                p2.executeUpdate();
            }
            try (PreparedStatement p3 = conn.prepareStatement("DELETE FROM alineacion_defensas WHERE usuario_id = ?")) {
                p3.setString(1, id);
                p3.executeUpdate();
            }
            try (PreparedStatement p4 = conn.prepareStatement("DELETE FROM alineacion_medios WHERE usuario_id = ?")) {
                p4.setString(1, id);
                p4.executeUpdate();
            }
            try (PreparedStatement p5 = conn.prepareStatement("DELETE FROM alineacion_delanteros WHERE usuario_id = ?")) {
                p5.setString(1, id);
                p5.executeUpdate();
            }
            try (PreparedStatement p6 = conn.prepareStatement("DELETE FROM usuario_plantilla WHERE usuario_id = ?")) {
                p6.setString(1, id);
                p6.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al borrar usuario: " + id, e);
        }
    }

    /* ===========================
       Métodos auxiliares
       =========================== */

    // Mapea un Usuario a partir de un ResultSet. Recibe la Connection para poder leer listas relacionadas
    private Usuario mapUsuario(ResultSet rs, Connection conn) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getString("id"));

        String tipoStr = rs.getString("tipo");
        if (tipoStr != null) {
            try {
                u.setTipo(TipoUsuario.valueOf(tipoStr));
            } catch (IllegalArgumentException iae) {
                // si por alguna razón el valor en BD no coincide con el enum, dejamos null o logueamos
                u.setTipo(null);
            }
        } else {
            u.setTipo(null);
        }

        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password"));

        //Double saldo = rs.getObject("saldo", Double.class);
        u.setSaldo(rs.getDouble("saldo"));
        //u.setSaldo(saldo == null ? 0.0 : saldo);

        u.setEquipo(rs.getString("equipo_id"));

        // cargar alineacion y plantilla (usa la misma conexión para eficiencia)
        u.setAlineacion(loadAlineacion(conn, u.getId()));
        u.setPlantilla(loadPlantilla(conn, u.getId()));

        return u;
    }

    // Guarda un usuario usando la conexión proporcionada (no cierra la conexión)
    private void saveUsingConnection(Connection conn, Usuario usuario) throws SQLException {
        String sql = """
            INSERT INTO usuarios (id, tipo, email, password, saldo, equipo_id)
            VALUES (?, ?, ?, ?, ?, ?)
            ON CONFLICT (id) DO UPDATE SET
                tipo = EXCLUDED.tipo,
                email = EXCLUDED.email,
                password = EXCLUDED.password,
                saldo = EXCLUDED.saldo,
                equipo = EXCLUDED.equipo
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario.getId());

            if (usuario.getTipo() != null) {
                ps.setString(2, usuario.getTipo().name());
            } else {
                ps.setNull(2, Types.VARCHAR);
            }

            ps.setString(3, usuario.getEmail());
            ps.setString(4, usuario.getPassword());

            /*
            if (usuario.getSaldo() == null) {
                ps.setNull(5, Types.NUMERIC);
            } else {
                ps.setDouble(5, usuario.getSaldo());
            }
            */
            ps.setDouble(5, usuario.getSaldo());
            ps.setString(6, usuario.getEquipo());
            ps.executeUpdate();
        }

        // Guardar alineación y plantilla (usa la misma conexión)
        saveAlineacion(conn, usuario);
        savePlantilla(conn, usuario);
    }

    // Guarda la alineación: elimina la previa y crea la nueva (formacion, portero y listas)
    private void saveAlineacion(Connection conn, Usuario usuario) throws SQLException {

        // borrar alineación previa
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM alineacion_posiciones WHERE usuario_id = ?")) {
            ps.setString(1, usuario.getId());
            ps.executeUpdate();
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM alineaciones WHERE usuario_id = ?")) {
            ps.setString(1, usuario.getId());
            ps.executeUpdate();
        }

        Alineacion a = usuario.getAlineacion();
        if (a == null) return;

        // insertar base
        try (PreparedStatement ps = conn.prepareStatement("""
        INSERT INTO alineaciones (usuario_id, formacion, portero)
        VALUES (?, ?, ?)
    """)) {
            ps.setString(1, usuario.getId());
            ps.setString(2, a.getFormacion());
            ps.setString(3, a.getPortero());
            ps.executeUpdate();
        }

        // insertar posiciones
        insertPosiciones(conn, usuario.getId(), "DEFENSA", a.getDefensas());
        insertPosiciones(conn, usuario.getId(), "MEDIO", a.getMedios());
        insertPosiciones(conn, usuario.getId(), "DELANTERO", a.getDelanteros());
    }

    private void insertPosiciones(
            Connection conn,
            String usuarioId,
            String tipo,
            List<String> jugadores
    ) throws SQLException {

        if (jugadores == null) return;

        String sql = """
        INSERT INTO alineacion_posiciones
        (usuario_id, posicion_tipo, orden, jugador_id)
        VALUES (?, ?, ?, ?)
    """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < jugadores.size(); i++) {
                ps.setString(1, usuarioId);
                ps.setString(2, tipo);
                ps.setInt(3, i);
                ps.setString(4, jugadores.get(i));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }



    private void savePlantilla(Connection conn, Usuario usuario) throws SQLException {
        // eliminar previa
        try (PreparedStatement del = conn.prepareStatement("DELETE FROM usuario_plantilla WHERE usuario_id = ?")) {
            del.setString(1, usuario.getId());
            del.executeUpdate();
        }

        // insertar nueva
        insertLista(conn, "usuario_plantilla", usuario.getId(), usuario.getPlantilla());
    }

    // Helper genérico para insertar pares (usuario_id, jugador_id) en tablas de listas
    // Esta es la función que antes llamé "insertLista": la incluyo aquí completa.
    private void insertLista(Connection conn, String table, String usuarioId, List<String> jugadores) throws SQLException {
        if (jugadores == null || jugadores.isEmpty()) return;

        String sql = "INSERT INTO " + table + " (usuario_id, jugador_id) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (String jugadorId : jugadores) {
                ps.setString(1, usuarioId);
                ps.setString(2, jugadorId);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    // Carga una lista (usuario_plantilla, alineacion_xxx)
    private List<String> loadLista(Connection conn, String table, String usuarioId) throws SQLException {
        List<String> lista = new ArrayList<>();
        String sql = "SELECT jugador_id FROM " + table + " WHERE usuario_id = ? ORDER BY jugador_id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(rs.getString("jugador_id"));
                }
            }
        }
        return lista;
    }

    // Carga la alineación (formacion, portero y arrays)
    private Alineacion loadAlineacion(Connection conn, String usuarioId) throws SQLException {

        String sql = """
        SELECT formacion, portero
        FROM alineaciones
        WHERE usuario_id = ?
    """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuarioId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                Alineacion a = new Alineacion();
                a.setFormacion(rs.getString("formacion"));
                a.setPortero(rs.getString("portero"));

                a.setDefensas(loadPosiciones(conn, usuarioId, "DEFENSA"));
                a.setMedios(loadPosiciones(conn, usuarioId, "MEDIO"));
                a.setDelanteros(loadPosiciones(conn, usuarioId, "DELANTERO"));

                return a;
            }
        }
    }

    private List<String> loadPosiciones(
            Connection conn,
            String usuarioId,
            String tipo
    ) throws SQLException {

        List<String> jugadores = new ArrayList<>();

        String sql = """
        SELECT jugador_id
        FROM alineacion_posiciones
        WHERE usuario_id = ?
          AND posicion_tipo = ?
        ORDER BY orden
    """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuarioId);
            ps.setString(2, tipo);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    jugadores.add(rs.getString("jugador_id"));
                }
            }
        }
        return jugadores;
    }

    private List<String> loadPlantilla(Connection conn, String usuarioId) throws SQLException {

        List<String> jugadores = new ArrayList<>();

        String sql = """
        SELECT jugador_id
        FROM plantillas
        WHERE usuario_id = ?
        ORDER BY jugador_id
    """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    jugadores.add(rs.getString("jugador_id"));
                }
            }
        }
        return jugadores;
    }



}
