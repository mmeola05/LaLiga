package com.liga.repository.postgres;

import com.liga.model.Equipo;
import com.liga.repository.dao.EquipoDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EquipoDAOImplPostgres implements EquipoDAO {

    private final Connection connection;

    public EquipoDAOImplPostgres() {
        this.connection = PostgresConnection.getInstance().getConnection();
    }

    @Override
    public List<Equipo> findAll() {
        List<Equipo> equipos = new ArrayList<>();
        String sql = "SELECT id, nombre FROM equipos";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                equipos.add(mapRowToEquipo(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return equipos;
    }

    @Override
    public Optional<Equipo> findById(String idEquipo) {
        String sql = "SELECT id, nombre FROM equipos WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, idEquipo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToEquipo(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public void save(Equipo equipo) {
        String sql = "INSERT INTO equipos (id, nombre) VALUES (?, ?) " +
                     "ON CONFLICT (id) DO UPDATE SET nombre = EXCLUDED.nombre";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, equipo.getId());
            stmt.setString(2, equipo.getNombre());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveAll(List<Equipo> equipos) {
        // Simple iteration for now. Batching would be better for performance but keeping it consistent.
        for (Equipo e : equipos) {
            save(e);
        }
    }

    @Override
    public void deleteById(String id) {
        String sql = "DELETE FROM equipos WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Equipo mapRowToEquipo(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String nombre = rs.getString("nombre");
        return new Equipo(id, nombre);
    }
}
