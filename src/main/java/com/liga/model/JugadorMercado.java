package com.liga.model;

import java.util.Objects;

public class JugadorMercado {

    private final String jugadorId;
    private double precioSalida;
    private String vendedor;
    private String id;

    public JugadorMercado(String jugadorId, double precioSalida, String vendedor, String id) {
        this.jugadorId = jugadorId;
        this.precioSalida = precioSalida;
        this.vendedor = vendedor;
        this.id = id;
    }

    public String getJugadorId() {
        return jugadorId;
    }

    public double getPrecioSalida() {
        return precioSalida;
    }

    public String getVendedor() {
        return vendedor;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JugadorMercado that = (JugadorMercado) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
