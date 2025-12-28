package com.liga.model;

import java.util.ArrayList;
import java.util.List;

public class Jornada {
  private int numJornada;
  private List<Partido> partidos;

  public Jornada(int numJornada) {
    this.numJornada = numJornada;
    this.partidos = new ArrayList<>();
  }

  public int getNumJornada() {
    return numJornada;
  }

  public void setNumJornada(int numJornada) {
    this.numJornada = numJornada;
  }

  public List<Partido> getPartidos() {
    return partidos;
  }

  public void setPartidos(List<Partido> partidos) {
    this.partidos = partidos;
  }

  public void addPartido(Partido partido) {
    this.partidos.add(partido);
  }
}
