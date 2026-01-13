package com.liga.service;

import com.liga.model.Equipo;
import com.liga.model.Jugador;
import com.liga.model.Partido;
import com.liga.model.Posicion;

import java.util.Random;

public class SimuladorRapido {

  private final Random random = new Random();

  public void simularPartido(Partido partido, Jugador[] jugadoresLocal, Jugador[] jugadoresVisitante) {
    // Calcula poderio

    double valLocal = calcularValoracionEquipo(jugadoresLocal);
    double valVisit = calcularValoracionEquipo(jugadoresVisitante);

    double total = valLocal + valVisit;
    double factorLocal = (total == 0) ? 0.5 : (valLocal / total);

    // Oportunidades

    int ocasionesLocal = 3 + random.nextInt(4) + (factorLocal > 0.55 ? 2 : 0);
    int ocasionesVisit = 3 + random.nextInt(4) + (factorLocal < 0.45 ? 2 : 0);

    int golesLocal = 0;
    int golesVisit = 0;

    // Ocasiones local
    for (int i = 0; i < ocasionesLocal; i++) {
      // Probabilidad gol
      // Base 15% + (diferencia de valoración / 200)
      double chance = 0.15 + ((valLocal - valVisit) / 200.0);
      if (chance < 0.05)
        chance = 0.05; // Mínimo 5%
      if (chance > 0.90)
        chance = 0.90; // Máximo 90%

      if (random.nextDouble() < chance)
        golesLocal++;
    }

    // Ocasiones visitante

    for (int i = 0; i < ocasionesVisit; i++) {
      // Base 12% (visitante) + ajuste
      double chance = 0.12 + ((valVisit - valLocal) / 200.0);
      if (chance < 0.05)
        chance = 0.05;
      if (chance > 0.90)
        chance = 0.90;

      if (random.nextDouble() < chance)
        golesVisit++;
    }

    partido.setGolesLocal(golesLocal);
    partido.setGolesVisitante(golesVisit);

    // Persistencia

    asignarGoles(partido, jugadoresLocal, golesLocal);
    asignarGoles(partido, jugadoresVisitante, golesVisit);

    // Actualiza estadisticas

    partido.getEquipoLocal().actualizarEstadisticas(golesLocal, golesVisit);
    partido.getEquipoVisitante().actualizarEstadisticas(golesVisit, golesLocal);
  }

  private void asignarGoles(Partido partido, Jugador[] jugadores, int cantidadGoles) {
    if (cantidadGoles == 0)
      return;

    java.util.List<Jugador> candidatos = new java.util.ArrayList<>();
    for (Jugador j : jugadores) {
      if (j != null)
        candidatos.add(j);
    }

    if (candidatos.isEmpty())
      return;

    for (int i = 0; i < cantidadGoles; i++) {
      Jugador scorer = candidatos.get(random.nextInt(candidatos.size()));
      int minutoRandom = 1 + random.nextInt(90);
      partido.addGol(new com.liga.model.Gol(scorer, minutoRandom));
    }
  }

  private double calcularValoracionEquipo(Jugador[] jugadores) {
    double total = 0;
    for (Jugador j : jugadores) {
      if (j == null)
        continue;
      total += calcularValoracionJugador(j);
    }
    return total;
  }

  private double calcularValoracionJugador(Jugador j) {
    // Formula
    int statPrincipal = 50;

    if (j.getPosicion() != null) {
      switch (j.getPosicion()) {
        case PORTERO:
          statPrincipal = j.getPorteria();
          break;
        case DEFENSA:
          statPrincipal = j.getDefensa();
          break;
        case MEDIO:
          statPrincipal = j.getPase();
          break;
        case DELANTERO:
          statPrincipal = j.getAtaque();
          break;
      }
    }

    double valor0to100 = (statPrincipal * 0.6) + (j.getCondition() * 0.4) + (random.nextDouble() * 4 - 2);
    return Math.max(0, valor0to100);
  }

  private int generarGolesPoisson(double lambda) {
    // Algoritmo Poisson

    double L = Math.exp(-lambda);
    double p = 1.0;
    int k = 0;

    do {
      k++;
      p *= random.nextDouble();
    } while (p > L);

    return k - 1;
  }
}
