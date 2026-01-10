package com.liga.repository.postgres;

import com.liga.model.JugadorMercado;
import com.liga.repository.dao.MarketDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MarketDAOImplPostgres implements MarketDAO {

    @Override
    public Optional<JugadorMercado> findJugadorMercadoById(String idJugadorMercado) {

        String sql = """
            SELECT id, jugador_id, precio_salida, vendedor_id
            FROM mercado
            WHERE id = ?
        """;

        try (Connection conn = PostgresConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, idJugadorMercado);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapJugadorMercado(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar jugador en mercado", e);
        }

        return Optional.empty();
    }

    @Override
    public List<JugadorMercado> findAllJugadoresMercados() {

        List<JugadorMercado> resultado = new ArrayList<>();

        String sql = """
            SELECT id, jugador_id, precio_salida, vendedor_id
            FROM mercado
        """;

        try (Connection conn = PostgresConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                resultado.add(mapJugadorMercado(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener mercado", e);
        }

        return resultado;
    }

    @Override
    public void saveJugadorMercado(JugadorMercado jugadorMercado) {

        String sql = """
            INSERT INTO mercado (id, jugador_id, precio_salida, vendedor_id)
            VALUES (?, ?, ?, ?)
            ON CONFLICT (id) DO UPDATE SET
                jugador_id = EXCLUDED.jugador_id,
                precio_salida = EXCLUDED.precio_salida,
                vendedor_id = EXCLUDED.vendedor_id
        """;

        try (Connection conn = PostgresConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, jugadorMercado.getId());
            ps.setString(2, jugadorMercado.getJugadorId());
            ps.setDouble(3, jugadorMercado.getPrecioSalida());
            ps.setString(4, jugadorMercado.getVendedor());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar jugador en mercado", e);
        }
    }

    @Override
    public void deleteJugadorMercadoById(String idJugadorMercado) {

        String sql = "DELETE FROM mercado WHERE id = ?";

        try (Connection conn = PostgresConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, idJugadorMercado);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al borrar jugador del mercado", e);
        }
    }

    /* ===========================
       Mapeo
       =========================== */

    private JugadorMercado mapJugadorMercado(ResultSet rs) throws SQLException {

        String id = rs.getString("id");
        String  jugadorId = rs.getString("jugador_id");
        double precio_salida =  rs.getDouble("precio_salida");
        String vendedor =  rs.getString("vendedor_id");

        return new JugadorMercado(jugadorId, precio_salida, vendedor, id);
    }
}
