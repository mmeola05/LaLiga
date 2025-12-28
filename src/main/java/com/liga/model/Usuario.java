package com.liga.model;

import java.util.List;
import java.util.Objects;

public class Usuario {
    private String id;
    private TipoUsuario tipo;
    private String email;
    private String password;
    private double saldo;
    private String equipo;
    private Alineacion alineacion;
    private List<String> plantilla;

    public Usuario() {}

    public Usuario(String id, TipoUsuario tipo, String email, String password, double saldo, String equipo, Alineacion alineacion) {
        this.id = id;
        this.tipo = tipo;
        this.email = email;
        this.password = password;
        this.saldo = saldo;
        this.equipo = equipo;
        this.alineacion = alineacion;
    }

    public String getId() {
        return id;
    }

    public TipoUsuario getTipo() {
        return tipo;
    }

    public void setTipo(TipoUsuario tipo) {
        this.tipo = tipo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    public String getEquipo() {
        return equipo;
    }

    public void setEquipo(String equipo) {
        this.equipo = equipo;
    }

    public Alineacion getAlineacion() {
        return alineacion;
    }

    public void setAlineacion(Alineacion alineacion) {
        this.alineacion = alineacion;
    }
    public List<String> getPlantilla() {
        return plantilla;
    }

    public void setPlantilla(List<String> plantilla) {
        this.plantilla = plantilla;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(id, usuario.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id='" + id + '\'' +
                ", tipo=" + tipo +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", saldo=" + saldo +
                ", equipo='" + equipo + '\'' +
                ", alineacion=" + alineacion +
                '}';
    }
}