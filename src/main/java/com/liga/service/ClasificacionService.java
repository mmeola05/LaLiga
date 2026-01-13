package com.liga.service;

import com.liga.model.Equipo;
import com.liga.model.Partido;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ClasificacionService {

  // Actualiza estadisticas segun los partidos

  public void actualizarClasificacion(List<Partido> partidos) {
    if (partidos == null || partidos.isEmpty()) {
      return;
    }

    for (Partido partido : partidos) {
      // Datos del partido

      int golesLocal = partido.getGolesLocal();
      int golesVisitante = partido.getGolesVisitante();

      // Actualiza local

      if (partido.getEquipoLocal() != null) {
        partido.getEquipoLocal().actualizarEstadisticas(golesLocal, golesVisitante);
      }

      // Actualiza visitante

      if (partido.getEquipoVisitante() != null) {
        partido.getEquipoVisitante().actualizarEstadisticas(golesVisitante, golesLocal);
      }
    }
  }

  // Ordena equipos por clasificacion

  public void ordenarClasificacion(List<Equipo> equipos) {
    if (equipos == null || equipos.isEmpty()) {
      return;
    }

    Collections.sort(equipos, new Comparator<Equipo>() {
      @Override
      public int compare(Equipo e1, Equipo e2) {
        // 1. Puntos

        int comparePuntos = Integer.compare(e2.getPuntos(), e1.getPuntos());
        if (comparePuntos != 0) {
          return comparePuntos;
        }

        // 2. Diferencia de goles

        int diffGol1 = e1.getGolesFavor() - e1.getGolesContra();
        int diffGol2 = e2.getGolesFavor() - e2.getGolesContra();
        int compareDiff = Integer.compare(diffGol2, diffGol1);
        if (compareDiff != 0) {
          return compareDiff;
        }

        // 3. Goles a favor

        int compareGF = Integer.compare(e2.getGolesFavor(), e1.getGolesFavor());
        if (compareGF != 0) {
          return compareGF;
        }

        // 4. Partidos ganados

        int compareWins = Integer.compare(e2.getVictorias(), e1.getVictorias());
        if (compareWins != 0) {
          return compareWins;
        }

        // 5. Orden alfabetico

        return e1.getNombre().compareToIgnoreCase(e2.getNombre());
      }
    });
  }

  // Imprime tabla de clasificacion

  public void imprimirClasificacion(List<Equipo> equipos, String userTeamId) {
    if (equipos == null || equipos.isEmpty()) {
      System.out.println("No hay equipos para mostrar en la clasificación.");
      return;
    }

    // Colores ANSI

    final String RESET = "\u001B[0m";
    final String GREEN_BOLD = "\u001B[1;32m";
    final String WHITE = "\u001B[0m";

    System.out.println("\n=========================================================================================");
    System.out.printf("%-4s %-25s | %3s  %3s  %3s  %3s | %3s  %3s  %4s | %3s%n",
        "Pos", "Equipo", "PJ", "PG", "PE", "PP", "GF", "GC", "DG", "Pts");
    System.out.println("-----------------------------------------------------------------------------------------");

    int posicion = 1;
    for (Equipo e : equipos) {
      int dg = e.getGolesFavor() - e.getGolesContra();

      boolean isUserTeam = userTeamId != null && userTeamId.equals(e.getId());

      // Marca equipo del usuario

      String color = isUserTeam ? GREEN_BOLD : WHITE;
      String mark = isUserTeam ? "* " : "";
      String nombreMostrar = mark + e.getNombre();
      // Ajuste visual

      if (isUserTeam) {
        System.out.print(GREEN_BOLD);
      }

      System.out.printf("%-4d %-25s | %3d  %3d  %3d  %3d | %3d  %3d  %4d | %3d%n",
          posicion++,
          nombreMostrar,

          e.getPartidosJugados(),
          e.getVictorias(),
          e.getEmpates(),
          e.getDerrotas(),
          e.getGolesFavor(),
          e.getGolesContra(),
          dg,
          e.getPuntos());

      if (isUserTeam) {
        System.out.print(RESET);
      }
    }
    System.out.println("=========================================================================================\n");
  }
}
