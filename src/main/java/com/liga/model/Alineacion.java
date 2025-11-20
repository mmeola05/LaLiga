package com.liga.model;

import java.util.List;

public class Alineacion {
    private String formacion;
    private String portero;
    private List<String> defensas;
    private List<String> medios;
    private List<String> delanteros;

    public Alineacion() {}

    public Alineacion(String formacion, String portero, List<String> defensas, List<String> medios, List<String> delanteros) {
        this.formacion = formacion;
        this.portero = portero;
        this.defensas = defensas;
        this.medios = medios;
        this.delanteros = delanteros;
    }

    public String getFormacion() {
        return formacion;
    }

    public void setFormacion(String formacion) {
        this.formacion = formacion;
    }

    public String getPortero() {
        return portero;
    }

    public void setPortero(String portero) {
        this.portero = portero;
    }

    public List<String> getDefensas() {
        return defensas;
    }

    public void setDefensas(List<String> defensas) {
        this.defensas = defensas;
    }

    public List<String> getMedios() {
        return medios;
    }

    public void setMedios(List<String> medios) {
        this.medios = medios;
    }

    public List<String> getDelanteros() {
        return delanteros;
    }

    public void setDelanteros(List<String> delanteros) {
        this.delanteros = delanteros;
    }

    @Override
    public String toString() {
        return "Alineacion{" +
                "formacion='" + formacion + '\'' +
                ", portero='" + portero + '\'' +
                ", defensas=" + defensas +
                ", medios=" + medios +
                ", delanteros=" + delanteros +
                '}';
    }
}
