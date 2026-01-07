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
import com.liga.model.Posicion;
import com.liga.repository.RepositoryFactory;

public class SimuladorJornada {
  private LeagueRepository leagueRepository;
  private Random random;
  private com.liga.service.SimuladorService simuladorService;

  public SimuladorJornada() {
    this.leagueRepository = RepositoryFactory.getLeagueRepository();
    this.random = new Random();
    this.simuladorService = new com.liga.service.SimuladorService();
  }

  public Jornada simularJornada(int numeroJornada, List<Partido> partidosPendientes, String idUsuario) {
    Jornada jornada = new Jornada(numeroJornada);
    // String idUsuario = obtenerIdUsuario(); // REMOVED: Usamos el parametro
    // explicito

    for (Partido partido : partidosPendientes) {
      simularPartido(partido, idUsuario);
      jornada.addPartido(partido);
    }
    return jornada;
  }

  private void simularPartido(Partido partido, String idUsuario) {
    // 1. Obtener jugadores para cada equipo (Titulares / 11 iniciales)
    Jugador[] localXI = obtenerOnceInicial(partido.getEquipoLocal(), idUsuario);
    Jugador[] visitXI = obtenerOnceInicial(partido.getEquipoVisitante(), idUsuario);

    // 2. Ejecutar simulación híbrida
    // El servicio decide si usa Pro o Rápido
    Partido resultado = simuladorService.jugarPartido(partido, localXI, visitXI, idUsuario);

    // (Opcional) Si el servicio devuelve una nueva instancia, actualizar la ref.
    // Pero como pasamos 'partido' por referencia, ya debería estar actualizado.
  }

  /**
   * Selecciona los 11 jugadores titulares.
   * - Si es equipo de Usuario: Usa su Alineacion.
   * - Si es IA: Selecciona los mejores por posición (1-4-3-3 genérico).
   */
  private Jugador[] obtenerOnceInicial(Equipo equipo, String idUsuarioEquipo) {
    Jugador[] once = new Jugador[11];
    List<Jugador> plantilla = leagueRepository.buscarJugadorPorEquipo(equipo.getId());

    if (plantilla.isEmpty())
      return once; // Retorna array con nulls

    // CHEQUEO SI ES EQUIPO USUARIO
    if (equipo.getId().equals(idUsuarioEquipo)) {
      // Buscar usuario
      Optional<Usuario> userOpt = leagueRepository.listarUsuarios().stream()
          .filter(u -> u.getEquipo() != null && u.getEquipo().equals(equipo.getId()))
          .findFirst();

      if (userOpt.isPresent() && userOpt.get().getAlineacion() != null) {
        return mapearAlineacion(userOpt.get().getAlineacion(), plantilla);
      }
    }

    // SELECCIÓN IA (Automática)
    // Estrategia simple: 1 Portero, 4 Defensas, 3 Medios, 3 Delanteros
    int idx = 0;

    // 1. Portero
    Jugador portero = buscarMejor(plantilla, Posicion.PORTERO, new HashSet<>());
    if (portero != null)
      once[idx++] = portero;

    // 2. Defensas (4)
    for (int i = 0; i < 4; i++) {
      if (idx < 11) {
        Jugador def = buscarMejor(plantilla, Posicion.DEFENSA, alinearSet(once));
        if (def != null)
          once[idx++] = def;
      }
    }

    // 3. Medios (3)
    for (int i = 0; i < 3; i++) {
      if (idx < 11) {
        Jugador med = buscarMejor(plantilla, Posicion.MEDIO, alinearSet(once));
        if (med != null)
          once[idx++] = med;
      }
    }

    // 4. Delanteros (3)
    for (int i = 0; i < 3; i++) {
      if (idx < 11) {
        Jugador del = buscarMejor(plantilla, Posicion.DELANTERO, alinearSet(once));
        if (del != null)
          once[idx++] = del;
      }
    }

    // Relleno si faltan (por si no hay suficientes de una pos)
    for (Jugador j : plantilla) {
      if (idx >= 11)
        break;
      if (!alinearSet(once).contains(j.getId())) {
        once[idx++] = j;
      }
    }

    return once;
  }

  private Jugador[] mapearAlineacion(Alineacion ali, List<Jugador> plantilla) {
    Jugador[] once = new Jugador[11];
    int idx = 0;

    // Portero
    if (ali.getPortero() != null)
      once[idx++] = findById(plantilla, ali.getPortero());

    // Defensas
    if (ali.getDefensas() != null) {
      for (String id : ali.getDefensas())
        if (idx < 11)
          once[idx++] = findById(plantilla, id);
    }
    // Medios
    if (ali.getMedios() != null) {
      for (String id : ali.getMedios())
        if (idx < 11)
          once[idx++] = findById(plantilla, id);
    }
    // Delanteros
    if (ali.getDelanteros() != null) {
      for (String id : ali.getDelanteros())
        if (idx < 11)
          once[idx++] = findById(plantilla, id);
    }

    return once;
  }

  private Jugador findById(List<Jugador> lista, String id) {
    return lista.stream().filter(j -> j.getId().equals(id)).findFirst().orElse(null);
  }

  private Jugador buscarMejor(List<Jugador> lista, Posicion pos, Set<String> usados) {
    return lista.stream()
        .filter(j -> j.getPosicion() == pos && !usados.contains(j.getId()))
        .max((a, b) -> Integer.compare(getMedia(a), getMedia(b)))
        .orElse(null);
  }

  private int getMedia(Jugador j) {
    // Simplificado
    return (j.getAtaque() + j.getDefensa() + j.getPase() + j.getPorteria()) / 4;
  }

  private Set<String> alinearSet(Jugador[] once) {
    Set<String> set = new HashSet<>();
    for (Jugador j : once) {
      if (j != null)
        set.add(j.getId());
    }
    return set;
  }

}
