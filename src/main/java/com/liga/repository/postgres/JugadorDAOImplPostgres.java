package com.liga.repository.postgres;

import com.liga.model.Jugador;
import com.liga.model.Posicion;
import com.liga.repository.dao.JugadorDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JugadorDAOImplPostgres implements JugadorDAO {

    private final Connection connection;

    public JugadorDAOImplPostgres() {
        this.connection = PostgresConnection.getInstance().getConnection();
    }

    @Override
    public List<Jugador> findAll() {
        return executeQuery("SELECT * FROM jugadores");
    }

    @Override
    public List<Jugador> findByEquipo(String idEquipo) {
        return executeQuery("SELECT * FROM jugadores WHERE equipo_id = ?", idEquipo);
    }

    @Override
    public Optional<Jugador> findById(String id) {
        List<Jugador> result = executeQuery("SELECT * FROM jugadores WHERE id = ?", id);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public void save(Jugador j) {
        String sql = """
            INSERT INTO jugadores (id, nombre, posicion, equipo_id, precio, ataque, defensa, pase, porteria, condicion)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (id) DO UPDATE SET
            nombre = EXCLUDED.nombre,
            posicion = EXCLUDED.posicion,
            equipo_id = EXCLUDED.equipo_id,
            precio = EXCLUDED.precio,
            ataque = EXCLUDED.ataque,
            defensa = EXCLUDED.defensa,
            pase = EXCLUDED.pase,
            porteria = EXCLUDED.porteria,
            condicion = EXCLUDED.condicion
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, j.getId());
            stmt.setString(2, j.getNombre());
            stmt.setString(3, j.getPosicion() != null ? j.getPosicion().name() : null);
            stmt.setString(4, j.getEquipoId());
            stmt.setDouble(5, j.getPrecio());
            stmt.setInt(6, j.getAtaque());
            stmt.setInt(7, j.getDefensa());
            stmt.setInt(8, j.getPase());
            stmt.setInt(9, j.getPorteria());
            stmt.setInt(10, j.getCondition());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveAll(List<Jugador> jugadores) {
        // Implementacion simple iterativa
        for (Jugador j : jugadores) {
            save(j);
        }
    }

    @Override
    public void deleteById(String id) {
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM jugadores WHERE id = ?")) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private List<Jugador> executeQuery(String sql, Object... params) {
        List<Jugador> list = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Jugador mapRow(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String nombre = rs.getString("nombre");
        String posStr = rs.getString("posicion");
        Posicion posicion = posStr != null ? Posicion.valueOf(posStr) : null;
        String equipoId = rs.getString("equipo_id");
        double precio = rs.getDouble("precio");
        int ataque = rs.getInt("ataque");
        int defensa = rs.getInt("defensa");
        int pase = rs.getInt("pase");
        int porteria = rs.getInt("porteria");
        int condicion = rs.getInt("condicion");

        return new Jugador(id, nombre, posicion, equipoId, precio, ataque, defensa, pase, porteria, condicion);
    }
}
