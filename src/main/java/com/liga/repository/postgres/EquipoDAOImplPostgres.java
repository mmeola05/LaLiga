package com.liga.repository.postgres;

import com.liga.model.Equipo;
import com.liga.repository.dao.EquipoDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EquipoDAOImplPostgres implements EquipoDAO {

    @Override
    public List<Equipo> findAll() {
        String sql = "SELECT id, nombre FROM equipos";
        List<Equipo> equipos = new ArrayList<>();

        try (Connection conn = PostgresConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Equipo equipo = new Equipo();
                equipo.setId(rs.getString("id"));
                equipo.setNombre(rs.getString("nombre"));
                equipos.add(equipo);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener todos los equipos", e);
        }

        return equipos;
    }

    @Override
    public Optional<Equipo> findById(String id) {
        String sql = "SELECT id, nombre FROM equipos WHERE id = ?";
        try (Connection conn = PostgresConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Equipo equipo = new Equipo();
                    equipo.setId(rs.getString("id"));
                    equipo.setNombre(rs.getString("nombre"));
                    return Optional.of(equipo);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar equipo por id: " + id, e);
        }

        return Optional.empty();
    }

    @Override
    public void save(Equipo equipo) {
        String sql = """
            INSERT INTO equipos (id, nombre)
            VALUES (?, ?)
            ON CONFLICT (id)
            DO UPDATE SET nombre = EXCLUDED.nombre
            """;

        try (Connection conn = PostgresConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, equipo.getId());
            stmt.setString(2, equipo.getNombre());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar equipo: " + equipo.getId(), e);
        }
    }

    @Override
    public void saveAll(List<Equipo> equipos) {
        String sql = """
            INSERT INTO equipos (id, nombre)
            VALUES (?, ?)
            ON CONFLICT (id)
            DO UPDATE SET nombre = EXCLUDED.nombre
            """;

        try (Connection conn = PostgresConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (Equipo equipo : equipos) {
                stmt.setString(1, equipo.getId());
                stmt.setString(2, equipo.getNombre());
                stmt.addBatch();
            }

            stmt.executeBatch();

        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar lista de equipos", e);
        }
    }

    @Override
    public void deleteById(String id) {
        String sql = "DELETE FROM equipos WHERE id = ?";

        try (Connection conn = PostgresConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar equipo con id: " + id, e);
        }
    }
}
