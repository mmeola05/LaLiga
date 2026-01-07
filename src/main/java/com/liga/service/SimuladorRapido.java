package com.liga.service;

import com.liga.model.Equipo;
import com.liga.model.Jugador;
import com.liga.model.Partido;
import com.liga.model.Posicion;

import java.util.Random;

public class SimuladorRapido {

  private final Random random = new Random();

  public void simularPartido(Partido partido, Jugador[] jugadoresLocal, Jugador[] jugadoresVisitante) {
    // Calculamos poderío de cada equipo
    double valLocal = calcularValoracionEquipo(jugadoresLocal);
    double valVisit = calcularValoracionEquipo(jugadoresVisitante);

    double total = valLocal + valVisit;
    double factorLocal = (total == 0) ? 0.5 : (valLocal / total);

    // Oportunidades: Entre 3 y 8 por equipo, ajustado por dominio
    // Dominio > 0.55 => +2 ocasiones.
    int ocasionesLocal = 3 + random.nextInt(4) + (factorLocal > 0.55 ? 2 : 0);
    int ocasionesVisit = 3 + random.nextInt(4) + (factorLocal < 0.45 ? 2 : 0);

    int golesLocal = 0;
    int golesVisit = 0;

    // Simular Ocasiones Local
    for (int i = 0; i < ocasionesLocal; i++) {
      // Probabilidad de gol: Calidad Local vs Calidad Visitante
      // Base 15% + (diferencia de valoración / 200)
      double chance = 0.15 + ((valLocal - valVisit) / 200.0);
      if (chance < 0.05)
        chance = 0.05; // Mínimo 5%
      if (chance > 0.90)
        chance = 0.90; // Máximo 90%

      if (random.nextDouble() < chance)
        golesLocal++;
    }

    // Simular Ocasiones Visitante
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

    // PERSISTENCIA DE GOLEADORES
    asignarGoles(partido, jugadoresLocal, golesLocal);
    asignarGoles(partido, jugadoresVisitante, golesVisit);

    // Actualizar estadísticas de equipos
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
    // Fórmula: ((posicion * 0.6) + (estadoForma * 0.4)) / 100 + random(-2, 2)
    // pos stat depends on position
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
          break; // O pase, o promedio
        case DELANTERO:
          statPrincipal = j.getAtaque();
          break;
      }
    }

    double base = (statPrincipal * 0.6) + (j.getCondition() * 0.4);
    // La fórmula original era "/ 100", lo que daría valores < 1.
    // Asumo que el user quiere decir que eso da un "rating" base bajo, o tal vez
    // quiere decir escalar.
    // Si stat es 90, condition 90 -> 90. / 100 -> 0.9. + random(-2, 2) -> result
    // puede ser negativo?
    // "La suma de los 11 jugadores da la valoración total del equipo."
    // Si cada jugador da ~1.0, el equipo da ~11.
    // Voy a seguir la fórmula literal, pero asumo que random es pequeño.

    double val = (base / 1.0) + (random.nextDouble() * 4 - 2);
    // User said: "/ 100". Esto haría los valores muy pequeños (0.9).
    // "random(-2, 2)" dominaría totalmente (rango 4 vs rango 1).
    // PROBABLEMENTE el usuario quería decir que el random se aplica AL FINAL sobre
    // una escala similar.
    // O QUIZAS "/ 1" y no "/ 100".
    // Voy a asumir que "/ 100" es para normalizar a un "coeficiente de calidad" (ej
    // 0.8), y el random(-2,2) es un error de magnitud en el prompt O se refiere a
    // random sobre el total.
    // RELEYENDO: "((posicion * 0.6) + (estadoForma * 0.4)) / 100 + random(-2, 2)."
    // Si stat=90 -> 54 + 36 = 90. /100 = 0.9.
    // random(-2, 2) -> -1.5. Resultado = -0.6. NO TIENE SENTIDO tener valoración
    // negativa.
    // INTERPRETACION: El random es sobre la stat (0-100) ANTES de dividir, O el
    // random es decimal pequeño (-0.2, 0.2).
    // VOY A AJUSTAR: El random será (-2, 2) aplicado a la MEDIA (0-100), y luego
    // TODO dividido o usado.
    // O mejor: Interpretamos el random como "puntos de varaicion de stat".
    // val = ((stat * 0.6 + form * 0.4) + random(-2, 2)) (Escala 0-100).

    double valor0to100 = (statPrincipal * 0.6) + (j.getCondition() * 0.4) + (random.nextDouble() * 4 - 2);
    return Math.max(0, valor0to100);
  }

  private int generarGolesPoisson(double lambda) {
    // Algoritmo simple para generar número aleatorio con dist Poisson
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
