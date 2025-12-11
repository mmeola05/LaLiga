package com.liga.model;

public class Gol {
  private Jugador jugador;
  private int minuto;

  public Gol(Jugador jugador, int minuto) {
    this.jugador = jugador;
    this.minuto = minuto;
  }

  public Jugador getJugador() {
    return jugador;
  }

  public void setJugador(Jugador jugador) {
    this.jugador = jugador;
  }

  public int getMinuto() {
    return minuto;
  }

  public void setMinuto(int minuto) {
    this.minuto = minuto;
  }
}
