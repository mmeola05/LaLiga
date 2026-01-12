package com.liga.repository.postgres;

import com.liga.model.JugadorMercado;
import com.liga.repository.dao.MarketDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MarketDAOImplPostgres implements MarketDAO {

    private final Connection connection;

    public MarketDAOImplPostgres() {
        this.connection = PostgresConnection.getInstance().getConnection();
    }

    @Override
    public List<JugadorMercado> findAllJugadoresMercados() {
        List<JugadorMercado> list = new ArrayList<>();
        String sql = "SELECT * FROM mercado";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Optional<JugadorMercado> findJugadorMercadoById(String id) {
        String sql = "SELECT * FROM mercado WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public void saveJugadorMercado(JugadorMercado m) {
        String sql = """
            INSERT INTO mercado (id, jugador_id, precio_salida, vendedor)
            VALUES (?, ?, ?, ?)
            ON CONFLICT (id) DO UPDATE SET
            jugador_id = EXCLUDED.jugador_id,
            precio_salida = EXCLUDED.precio_salida,
            vendedor = EXCLUDED.vendedor
        """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, m.getId());
            stmt.setString(2, m.getJugadorId());
            stmt.setDouble(3, m.getPrecioSalida());
            stmt.setString(4, m.getVendedor());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteJugadorMercadoById(String id) {
        String sql = "DELETE FROM mercado WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private JugadorMercado mapRow(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String jugadorId = rs.getString("jugador_id");
        double precio = rs.getDouble("precio_salida");
        String vendedor = rs.getString("vendedor");
        // FIX: Constructor is (jugadorId, precio, vendedor, id)
        return new JugadorMercado(jugadorId, precio, vendedor, id);
    }
}
