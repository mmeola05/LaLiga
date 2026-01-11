package com.liga.model;

import java.util.Objects;

public class Equipo {

    private String id;
    private String nombre;

    // Estadísticas de clasificación
    private int partidosJugados;
    private int victorias;
    private int empates;
    private int derrotas;
    private int golesFavor;
    private int golesContra;
    private int puntos;

    public Equipo() {
    }

    public Equipo(String id, String nombre) {
        this.id = id;
        this.nombre = nombre;
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

    public void setId(String id) {this.id = id;}

    // ============================================================
    // MÉTODOS DE GESTIÓN DE ESTADÍSTICAS
    // ============================================================

    /**
     * Actualiza las estadísticas del equipo tras jugar un partido.
     *
     * @param golesAnotados  Goles marcados por este equipo.
     * @param golesRecibidos Goles recibidos por este equipo.
     */
    public void actualizarEstadisticas(int golesAnotados, int golesRecibidos) {
        this.partidosJugados++;
        this.golesFavor += golesAnotados;
        this.golesContra += golesRecibidos;

        if (golesAnotados > golesRecibidos) {
            this.victorias++;
            this.puntos += 3;
        } else if (golesAnotados == golesRecibidos) {
            this.empates++;
            this.puntos += 1; // 1 punto por empate
        } else {
            this.derrotas++;
        }
    }

    // ============================================================
    // GETTERS Y SETTERS (Estadísticas)
    // ============================================================

    public int getPartidosJugados() {
        return partidosJugados;
    }

    public void setPartidosJugados(int partidosJugados) {
        this.partidosJugados = partidosJugados;
    }

    public int getVictorias() {
        return victorias;
    }

    public void setVictorias(int victorias) {
        this.victorias = victorias;
    }

    public int getEmpates() {
        return empates;
    }

    public void setEmpates(int empates) {
        this.empates = empates;
    }

    public int getDerrotas() {
        return derrotas;
    }

    public void setDerrotas(int derrotas) {
        this.derrotas = derrotas;
    }

    public int getGolesFavor() {
        return golesFavor;
    }

    public void setGolesFavor(int golesFavor) {
        this.golesFavor = golesFavor;
    }

    public int getGolesContra() {
        return golesContra;
    }

    public void setGolesContra(int golesContra) {
        this.golesContra = golesContra;
    }

    public int getPuntos() {
        return puntos;
    }

    public void setPuntos(int puntos) {
        this.puntos = puntos;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        Equipo equipo = (Equipo) o;
        return Objects.equals(id, equipo.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return nombre + " (" + id + ")";
    }
}
