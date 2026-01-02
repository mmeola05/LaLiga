package com.liga.service;

import com.liga.model.Equipo;
import com.liga.model.Partido;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ClasificacionService {

  /**
   * Recorre la lista de partidos y actualiza las estadísticas de los equipos
   * participantes utilizando el método actualizarEstadisticas() de la clase
   * Equipo.
   *
   * @param partidos Lista de partidos jugados.
   */
  public void actualizarClasificacion(List<Partido> partidos) {
    if (partidos == null || partidos.isEmpty()) {
      return;
    }

    for (Partido partido : partidos) {
      // Obtener datos del partido
      int golesLocal = partido.getGolesLocal();
      int golesVisitante = partido.getGolesVisitante();

      // Actualizar estadísticas del equipo local
      if (partido.getEquipoLocal() != null) {
        partido.getEquipoLocal().actualizarEstadisticas(golesLocal, golesVisitante);
      }

      // Actualizar estadísticas del equipo visitante (invirtiendo marcadores)
      if (partido.getEquipoVisitante() != null) {
        partido.getEquipoVisitante().actualizarEstadisticas(golesVisitante, golesLocal);
      }
    }
  }

  /**
   * Ordena una lista de equipos según la clasificación:
   * 1. Puntos (Mayor a menor)
   * 2. Diferencia de goles (Mayor a menor)
   * 3. Goles a favor (Mayor a menor)
   *
   * @param equipos Lista de equipos a ordenar
   */
  public void ordenarClasificacion(List<Equipo> equipos) {
    if (equipos == null || equipos.isEmpty()) {
      return;
    }

    Collections.sort(equipos, new Comparator<Equipo>() {
      @Override
      public int compare(Equipo e1, Equipo e2) {
        // 1. Puntos (Descendente)
        int comparePuntos = Integer.compare(e2.getPuntos(), e1.getPuntos());
        if (comparePuntos != 0) {
          return comparePuntos;
        }

        // 2. Diferencia de goles (Descendente)
        int diffGol1 = e1.getGolesFavor() - e1.getGolesContra();
        int diffGol2 = e2.getGolesFavor() - e2.getGolesContra();
        int compareDiff = Integer.compare(diffGol2, diffGol1);
        if (compareDiff != 0) {
          return compareDiff;
        }

        // 3. Goles a favor (Descendente)
        return Integer.compare(e2.getGolesFavor(), e1.getGolesFavor());
      }
    });
  }

  /**
   * Imprime la tabla de clasificación formateada por consola.
   * Se asume que la lista de equipos ya viene ordenada.
   *
   * @param equipos    Lista de equipos a mostrar
   * @param userTeamId ID del equipo del usuario logueado (para resaltar). Puede
   *                   ser null.
   */
  public void imprimirClasificacion(List<Equipo> equipos, String userTeamId) {
    if (equipos == null || equipos.isEmpty()) {
      System.out.println("No hay equipos para mostrar en la clasificación.");
      return;
    }

    // Códigos ANSI para colores
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

      // Si es el equipo del usuario, ponemos color verde y un asterisco '*'
      String color = isUserTeam ? GREEN_BOLD : WHITE;
      String mark = isUserTeam ? "* " : "";
      String nombreMostrar = mark + e.getNombre();
      // Ajustamos el padding si añadimos caracteres
      // (Es complicado ajustar padding con ANSI, así que simplificamos: el nombre se
      // imprime normal,
      // pero si es el usuario, toda la línea sale verde).

      if (isUserTeam) {
        System.out.print(GREEN_BOLD);
      }

      System.out.printf("%-4d %-25s | %3d  %3d  %3d  %3d | %3d  %3d  %4d | %3d%n",
          posicion++,
          nombreMostrar, // nombre con marca si aplica
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
