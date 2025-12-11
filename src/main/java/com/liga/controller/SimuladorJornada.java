package com.liga.controller;

import java.util.List;
import java.util.Random;

import com.liga.model.Equipo;
import com.liga.model.Gol;
import com.liga.model.Jornada;
import com.liga.model.Jugador;
import com.liga.model.Partido;
import com.liga.repository.LeagueRepository;
import com.liga.repository.RepositoryFactory;

public class SimuladorJornada {
  private LeagueRepository leagueRepository;
  private Random random;

  public SimuladorJornada() {
    this.leagueRepository = RepositoryFactory.getLeagueRepository();
    this.random = new Random();
  }

  public Jornada simularJornada(int numeroJornada, List<Partido> partidosPendientes) {
    Jornada jornada = new Jornada(numeroJornada);

    for (Partido partido : partidosPendientes) {
      simularPartido(partido);
      jornada.addPartido(partido);
    }
    return jornada;
  }

  private void simularPartido(Partido partido) {
    // Simular goles aleatorios para cadad equipo (entre 0 y 5 goles)
    int golesLocal = random.nextInt(6);
    int golesVisitante = random.nextInt(6);

    partido.setGolesLocal(golesLocal);
    partido.setGolesVisitante(golesVisitante);

    generarGolesDetalle(partido, partido.getEquipoLocal(), golesLocal);
    generarGolesDetalle(partido, partido.getEquipoVisitante(), golesVisitante);

  }

  private void generarGolesDetalle(Partido partido, Equipo equipo, int numGoles) {
    List<Jugador> jugadoresEquipo = leagueRepository.buscarJugadorPorEquipo(equipo.getId());

    if (jugadoresEquipo.isEmpty()) {
      System.out.println("Advertencia: no hay jugadores en el equipo " + equipo.getNombre() + " para generar goles ");
      return;
    }
    for (int i = 0; i < numGoles; i++) {
      // Seleccionar un jugador aleatorio del equipo
      Jugador goleador = jugadoresEquipo.get(random.nextInt(jugadoresEquipo.size()));
      // Seleccionar un minuto aleatorio (entre 1 y 90)
      int minutoGol = random.nextInt(90) + 1;
      partido.addGol(new Gol(goleador, minutoGol));
    }
  }
}
