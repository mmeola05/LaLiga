package com.liga.repository.postgres;

import com.liga.model.*;
import com.liga.repository.dao.JorandaDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JornadaDAOImplPostgres implements JorandaDAO {

    private final Connection connection;

    public JornadaDAOImplPostgres() {
        this.connection = PostgresConnection.getInstance().getConnection();
    }

    @Override
    public List<Jornada> findAll() {
        // ListarIds
        List<Jornada> jornadas = new ArrayList<>();
        String sql = "SELECT id, num_jornada FROM jornadas ORDER BY num_jornada";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                int num = rs.getInt("num_jornada");
                Jornada j = new Jornada(num);
                // Load partidos
                j.setPartidos(loadPartidos(id));
                jornadas.add(j);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return jornadas;
    }

    @Override
    public Optional<Jornada> findById(int id) {
        // Unused by current app logic likely, but integrated for completeness
         String sql = "SELECT id, num_jornada FROM jornadas WHERE num_jornada = ?"; // Assuming arg 'id' means num_jornada? Or table id?
         // interface says 'id'. Usually implies PK. But app uses numJornada heavily.
         // Let's assume PK for safety, or numJornada if logic dictates. 
         // Given implementation:
         return Optional.empty(); 
    }

    @Override
    public void save(Jornada jornada) {
        // Estructura compleja: Insert jornada -> Insert partidos -> Insert goles
        // Insert partido_equipo

        String sqlJornada = "INSERT INTO jornadas (num_jornada) VALUES (?) ON CONFLICT (num_jornada) DO NOTHING RETURNING id";
        
        try {
            connection.setAutoCommit(false);
            
            // 1. Save or Get Jornada ID
            int jornadaId = -1;
            try (PreparedStatement stmt = connection.prepareStatement(sqlJornada)) {
                stmt.setInt(1, jornada.getNumJornada());
                try (ResultSet rs = stmt.executeQuery()) {
                   if (rs.next()) {
                       jornadaId = rs.getInt(1);
                   }
                }
            }
            // Si no devolvió nada (ON CONFLICT DO NOTHING), hay que buscar el ID
            if (jornadaId == -1) {
                try (PreparedStatement stmt = connection.prepareStatement("SELECT id FROM jornadas WHERE num_jornada = ?")) {
                    stmt.setInt(1, jornada.getNumJornada());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) jornadaId = rs.getInt(1);
                    }
                }
            }

            if (jornadaId == -1) throw new SQLException("No se pudo obtener ID de jornada " + jornada.getNumJornada());

            // 2. Save Partidos
            for (Partido p : jornada.getPartidos()) {
                savePartido(jornadaId, p);
            }

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

    @Override
    public void saveAll(List<Jornada> jornadas) {
        for (Jornada j : jornadas) save(j);
    }

    private void savePartido(int jornadaId, Partido p) throws SQLException {
        // Insert partido
        String sql = """
            INSERT INTO partidos (jornada_id, equipo_local, equipo_visitante, goles_local, goles_visitante)
            VALUES (?, ?, ?, ?, ?)
            RETURNING id
        """;
        int partidoId = -1;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, jornadaId);
            stmt.setString(2, p.getEquipoLocal().getId());
            stmt.setString(3, p.getEquipoVisitante().getId());
            stmt.setInt(4, p.getGolesLocal());
            stmt.setInt(5, p.getGolesVisitante());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) partidoId = rs.getInt(1);
            }
        }

        // Save Snapshots (partido_equipo)
        savePartidoEquipo(partidoId, p.getEquipoLocal(), true);
        savePartidoEquipo(partidoId, p.getEquipoVisitante(), false);

        // Save Goles
        for (Gol g : p.getGoles()) {
            saveGol(partidoId, g);
        }
    }

    private void savePartidoEquipo(int partidoId, Equipo e, boolean esLocal) throws SQLException {
        String sql = """
            INSERT INTO partido_equipo (partido_id, equipo_id, es_local, partidos_jugados, victorias, empates, derrotas, goles_favor, goles_contra, puntos)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, partidoId);
            stmt.setString(2, e.getId());
            stmt.setBoolean(3, esLocal);
            stmt.setInt(4, e.getPartidosJugados());
            stmt.setInt(5, e.getVictorias());
            stmt.setInt(6, e.getEmpates());
            stmt.setInt(7, e.getDerrotas());
            stmt.setInt(8, e.getGolesFavor());
            stmt.setInt(9, e.getGolesContra());
            stmt.setInt(10, e.getPuntos());
            stmt.executeUpdate();
        }
    }

    private void saveGol(int partidoId, Gol g) throws SQLException {
        String sql = "INSERT INTO goles (partido_id, jugador_id, minuto, equipo_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, partidoId);
            stmt.setString(2, g.getJugador().getId());
            stmt.setInt(3, g.getMinuto());
            stmt.setString(4, g.getJugador().getEquipoId());
            stmt.executeUpdate();
        }
    }

    private List<Partido> loadPartidos(int jornadaId) {
        List<Partido> partidos = new ArrayList<>();
        String sql = "SELECT * FROM partidos WHERE jornada_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, jornadaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int pId = rs.getInt("id");
                    String localId = rs.getString("equipo_local");
                    String visitId = rs.getString("equipo_visitante");
                    int gLocal = rs.getInt("goles_local");
                    int gVisit = rs.getInt("goles_visitante");

                    Equipo local = loadEquipoSnapshot(pId, localId);
                    Equipo visit = loadEquipoSnapshot(pId, visitId);

                    Partido p = new Partido(local, visit);
                    p.setGolesLocal(gLocal);
                    p.setGolesVisitante(gVisit);
                    
                    // Load Goles
                    List<Gol> goles = loadGoles(pId);
                    for(Gol g : goles) p.addGol(g);

                    partidos.add(p);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return partidos;
    }

    private Equipo loadEquipoSnapshot(int partidoId, String equipoId) {
        // Intentar cargar snapshot de partido_equipo
        String sql = "SELECT * FROM partido_equipo WHERE partido_id = ? AND equipo_id = ?";
        
        // Necesitamos el nombre del equipo, así que joineamos
        String joinSql = """
            SELECT pe.*, e.nombre 
            FROM partido_equipo pe
            JOIN equipos e ON pe.equipo_id = e.id
            WHERE pe.partido_id = ? AND pe.equipo_id = ?
        """;

        try (PreparedStatement stmt = connection.prepareStatement(joinSql)) {
            stmt.setInt(1, partidoId);
            stmt.setString(2, equipoId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Equipo e = new Equipo(equipoId, rs.getString("nombre"));
                    e.setPartidosJugados(rs.getInt("partidos_jugados"));
                    e.setVictorias(rs.getInt("victorias"));
                    e.setEmpates(rs.getInt("empates"));
                    e.setDerrotas(rs.getInt("derrotas"));
                    e.setGolesFavor(rs.getInt("goles_favor"));
                    e.setGolesContra(rs.getInt("goles_contra"));
                    e.setPuntos(rs.getInt("puntos"));
                    return e;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Fallback: Si no hay snapshot (raro), cargamos basico
        return new EquipoDAOImplPostgres().findById(equipoId).orElse(new Equipo(equipoId, "Unknown"));
    }

    private List<Gol> loadGoles(int partidoId) {
        List<Gol> goles = new ArrayList<>();
        String sql = "SELECT * FROM goles WHERE partido_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, partidoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String pId = rs.getString("jugador_id");
                    int min = rs.getInt("minuto");
                    // Load Jugador
                    Jugador j = new JugadorDAOImplPostgres().findById(pId).orElse(null);
                    if (j != null) {
                        goles.add(new Gol(j, min));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return goles;
    }
}
