package com.liga.model;

import java.util.Objects;

public class Jugador {

    private String id;
    private String nombre;
    private Posicion posicion;
    private String equipoId;

    private double precio;

    private int ataque;
    private int defensa;
    private int pase;
    private int porteria;

    private int condition;

    public Jugador() {
    }

    public Jugador(String id, String nombre, Posicion posicion, String equipoId, double precio, int ataque, int defensa, int pase, int porteria, int condition) {
        this.id = id;
        this.nombre = nombre;

        this.posicion = posicion;
        this.equipoId = equipoId;
        this.precio = precio;
        this.ataque = ataque;
        this.defensa = defensa;
        this.pase = pase;
        this.porteria = porteria;
        this.condition = condition;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Posicion getPosicion() {
        return posicion;
    }

    public void setPosicion(Posicion posicion) {
        this.posicion = posicion;
    }

    public String getEquipoId() {
        return equipoId;
    }

    public void setEquipoId(String equipoId) {
        this.equipoId = equipoId;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public int getAtaque() {
        return ataque;
    }

    public void setAtaque(int ataque) {
        this.ataque = ataque;
    }

    public int getDefensa() {
        return defensa;
    }

    public void setDefensa(int defensa) {
        this.defensa = defensa;
    }

    public int getPase() {
        return pase;
    }

    public void setPase(int pase) {
        this.pase = pase;
    }

    public int getPorteria() {
        return porteria;
    }

    public void setPorteria(int porteria) {
        this.porteria = porteria;
    }

    public int getCondition() {
        return condition;
    }

    public void setCondition(int condition) {
        this.condition = condition;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Jugador jugador = (Jugador) o;
        return Objects.equals(id, jugador.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Jugador{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", posicion=" + posicion +
                ", equipoId='" + equipoId + '\'' +
                ", precio=" + precio +
                ", ataque=" + ataque +
                ", defensa=" + defensa +
                ", pase=" + pase +
                ", porteria=" + porteria +
                ", condition=" + condition +
                '}';
    }
}
