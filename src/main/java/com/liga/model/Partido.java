package com.liga.model;

import java.util.ArrayList;
import java.util.List;

public class Partido {
  private Equipo equipoLocal;
  private Equipo equipoVisitante;
  private int golesLocal;
  private int golesVisitante;
  private List<Gol> goles = new ArrayList<>();

  public Partido(Equipo equipoLocal, Equipo equipoVisitante) {
    this.equipoLocal = equipoLocal;
    this.equipoVisitante = equipoVisitante;
  }

  public Equipo getEquipoLocal() {
    return equipoLocal;
  }

  public void setEquipoLocal(Equipo equipoLocal) {
    this.equipoLocal = equipoLocal;
  }

  public Equipo getEquipoVisitante() {
    return equipoVisitante;
  }

  public void setEquipoVisitante(Equipo equipoVisitante) {
    this.equipoVisitante = equipoVisitante;
  }

  public int getGolesLocal() {
    return golesLocal;
  }

  public void setGolesLocal(int golesLocal) {
    this.golesLocal = golesLocal;
  }

  public int getGolesVisitante() {
    return golesVisitante;
  }

  public void setGolesVisitante(int golesVisitante) {
    this.golesVisitante = golesVisitante;
  }

  public List<Gol> getGoles() {
    return goles;
  }

  public void addGol(Gol gol) {
    this.goles.add(gol);
  }

}
