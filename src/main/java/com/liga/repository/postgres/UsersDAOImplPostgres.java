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

    private final Connection connection;

    public UsersDAOImplPostgres() {
        this.connection = PostgresConnection.getInstance().getConnection();
    }

    @Override
    public List<Usuario> findAll() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM usuarios";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                usuarios.add(mapRowToUsuario(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return usuarios;
    }

    @Override
    public Optional<Usuario> findById(String id) {
        String sql = "SELECT * FROM usuarios WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUsuario(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public void save(Usuario u) {
        String sql = """
            INSERT INTO usuarios (id, tipo, email, password, saldo, equipo_id)
            VALUES (?, ?, ?, ?, ?, ?)
            ON CONFLICT (id) DO UPDATE SET
            tipo = EXCLUDED.tipo,
            email = EXCLUDED.email,
            password = EXCLUDED.password,
            saldo = EXCLUDED.saldo,
            equipo_id = EXCLUDED.equipo_id
        """;

        try {
            // Transactional (simple)
            connection.setAutoCommit(false);

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, u.getId());
                stmt.setString(2, u.getTipo() != null ? u.getTipo().name() : null);
                stmt.setString(3, u.getEmail());
                stmt.setString(4, u.getPassword());
                stmt.setDouble(5, u.getSaldo());
                stmt.setString(6, u.getEquipo());
                stmt.executeUpdate();
            }

            saveAlineacion(u);
            savePlantilla(u);

            connection.commit();

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveAlineacion(Usuario u) throws SQLException {
        if (u.getAlineacion() == null) return;
        Alineacion al = u.getAlineacion();

        // 1. Alineacion Header
        String sqlAli = """
            INSERT INTO alineaciones (usuario_id, formacion, portero)
            VALUES (?, ?, ?)
            ON CONFLICT (usuario_id) DO UPDATE SET
            formacion = EXCLUDED.formacion,
            portero = EXCLUDED.portero
        """;
        try (PreparedStatement stmt = connection.prepareStatement(sqlAli)) {
            stmt.setString(1, u.getId());
            stmt.setString(2, al.getFormacion());
            stmt.setString(3, al.getPortero());
            stmt.executeUpdate();
        }

        // 2. Posiciones (Delete all then insert)
        // Eliminamos posiciones previas para evitar conflictos o duplicados si cambió la alineación
        String delSql = "DELETE FROM alineacion_posiciones WHERE usuario_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(delSql)) {
            stmt.setString(1, u.getId());
            stmt.executeUpdate();
        }

        insertBatchPosiciones(u.getId(), "DEFENSA", al.getDefensas());
        insertBatchPosiciones(u.getId(), "MEDIO", al.getMedios());
        insertBatchPosiciones(u.getId(), "DELANTERO", al.getDelanteros());
    }

    private void insertBatchPosiciones(String userId, String tipo, List<String> jugadores) throws SQLException {
        if (jugadores == null || jugadores.isEmpty()) return;
        String sql = "INSERT INTO alineacion_posiciones (usuario_id, posicion_tipo, orden, jugador_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < jugadores.size(); i++) {
                stmt.setString(1, userId);
                stmt.setString(2, tipo);
                stmt.setInt(3, i + 1); // 1-indexed sort order
                stmt.setString(4, jugadores.get(i));
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private void savePlantilla(Usuario u) throws SQLException {
        if (u.getPlantilla() == null) return;
        
        // Delete previous
        String delSql = "DELETE FROM plantillas WHERE usuario_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(delSql)) {
            stmt.setString(1, u.getId());
            stmt.executeUpdate();
        }

        // Insert new
        String sql = "INSERT INTO plantillas (usuario_id, jugador_id) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (String pid : u.getPlantilla()) {
                stmt.setString(1, u.getId());
                stmt.setString(2, pid);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    @Override
    public void saveAll(List<Usuario> usuarios) {
        for (Usuario u : usuarios) save(u);
    }

    @Override
    public void deleteById(String id) {
        // Cascade delete handled by DB FKs usually, but let's be safe
        String sql = "DELETE FROM usuarios WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Usuario mapRowToUsuario(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getString("id"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password"));
        u.setSaldo(rs.getDouble("saldo"));
        u.setEquipo(rs.getString("equipo_id"));
        String tipoStr = rs.getString("tipo");
        if (tipoStr != null) u.setTipo(TipoUsuario.valueOf(tipoStr));

        // Load aggregations
        u.setAlineacion(loadAlineacion(u.getId()));
        u.setPlantilla(loadPlantilla(u.getId()));
        return u;
    }

    private Alineacion loadAlineacion(String userId) {
        Alineacion al = new Alineacion();
        // Load Header
        String sqlHeader = "SELECT formacion, portero FROM alineaciones WHERE usuario_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sqlHeader)) {
            stmt.setString(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    al.setFormacion(rs.getString("formacion"));
                    al.setPortero(rs.getString("portero"));
                } else {
                    return null; // No lineup found
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        // Load Positions
        al.setDefensas(loadPosiciones(userId, "DEFENSA"));
        al.setMedios(loadPosiciones(userId, "MEDIO"));
        al.setDelanteros(loadPosiciones(userId, "DELANTERO"));
        return al;
    }

    private List<String> loadPosiciones(String userId, String tipo) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT jugador_id FROM alineacion_posiciones WHERE usuario_id = ? AND posicion_tipo = ? ORDER BY orden";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setString(2, tipo);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getString("jugador_id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private List<String> loadPlantilla(String userId) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT jugador_id FROM plantillas WHERE usuario_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getString("jugador_id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
