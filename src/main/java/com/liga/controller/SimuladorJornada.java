package com.liga.controller;

import java.util.List;
import java.util.Random;

import com.liga.model.Equipo;
import com.liga.model.Gol;
import com.liga.model.Jornada;
import com.liga.model.Jugador;
import com.liga.model.Partido;
import com.liga.repository.LeagueRepository;
import java.util.stream.Collectors;
import com.liga.model.Usuario;
import com.liga.model.Alineacion;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
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
    // Obtener todos los jugadores que pertenecen al equipo actualmente
    List<Jugador> jugadoresEquipo = leagueRepository.buscarJugadorPorEquipo(equipo.getId());

    if (jugadoresEquipo.isEmpty()) {
      System.out.println("Advertencia: no hay jugadores en el equipo " + equipo.getNombre() + " para generar goles ");
      return;
    }

    // LISTA FINAL DE POTENCIALES GOLEADORES
    List<Jugador> candidatosGol = new ArrayList<>(jugadoresEquipo);

    // Verificar si este equipo es controlado por un USUARIO
    List<Usuario> usuarios = leagueRepository.listarUsuarios();
    Optional<Usuario> manager = usuarios.stream()
        .filter(u -> u.getEquipo() != null && u.getEquipo().equals(equipo.getId()))
        .findFirst();

    // Si tiene manager humano, filtramos por la alineacion titular
    if (manager.isPresent()) {
      Usuario u = manager.get();
      Alineacion ali = u.getAlineacion();

      if (ali != null) {
        Set<String> titularesIds = new HashSet<>();
        if (ali.getPortero() != null)
          titularesIds.add(ali.getPortero());
        if (ali.getDefensas() != null)
          titularesIds.addAll(ali.getDefensas());
        if (ali.getMedios() != null)
          titularesIds.addAll(ali.getMedios());
        if (ali.getDelanteros() != null)
          titularesIds.addAll(ali.getDelanteros());

        // Filtramos: solo dejamos en 'candidatosGol' a los que sean titulares
        List<Jugador> soloTitulares = jugadoresEquipo.stream()
            .filter(j -> titularesIds.contains(j.getId()))
            .collect(Collectors.toList());

        // Solo aplicamos el filtro si hay titulares definidos (por seguridad)
        if (!soloTitulares.isEmpty()) {
          candidatosGol = soloTitulares;
        }
      }
    }

    // Generar los goles usando la lista filtrada (o completa si es CPU)
    for (int i = 0; i < numGoles; i++) {
      if (candidatosGol.isEmpty())
        break; // Seguridad extra

      // Seleccionar un jugador aleatorio de los candidatos
      Jugador goleador = candidatosGol.get(random.nextInt(candidatosGol.size()));
      // Seleccionar un minuto aleatorio (entre 1 y 90)
      int minutoGol = random.nextInt(90) + 1;
      partido.addGol(new Gol(goleador, minutoGol));
    }
  }
}
