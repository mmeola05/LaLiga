package com.liga.repository.postgres;

import com.liga.model.Jornada;
import com.liga.repository.dao.JorandaDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JornadaDAOImplPostgres implements JorandaDAO {

    @Override
    public List<Jornada> findAll() {

        List<Jornada> jornadas = new ArrayList<>();

        String sql = "SELECT num_jornada FROM jornadas ORDER BY num_jornada";

        try (Connection conn = PostgresConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int numJornada = rs.getInt("num_jornada");
                jornadas.add(new Jornada(numJornada));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener jornadas", e);
        }

        return jornadas;
    }

    @Override
    public Optional<Jornada> findById(int id) {

        String sql = "SELECT num_jornada FROM jornadas WHERE num_jornada = ?";

        try (Connection conn = PostgresConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Jornada(rs.getInt("num_jornada")));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar jornada", e);
        }

        return Optional.empty();
    }

    @Override
    public void save(Jornada jornada) {
        saveAll(List.of(jornada));
    }

    @Override
    public void saveAll(List<Jornada> jornadas) {

        String sql = """
        INSERT INTO jornadas (num_jornada)
        VALUES (?)
        ON CONFLICT (num_jornada) DO NOTHING
    """;

        try (Connection conn = PostgresConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (Jornada jornada : jornadas) {
                ps.setInt(1, jornada.getNumJornada());
                ps.addBatch();
            }

            ps.executeBatch();

        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar jornadas", e);
        }
    }

}
