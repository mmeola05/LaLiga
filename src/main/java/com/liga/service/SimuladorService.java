package com.liga.service;

import com.liga.model.Equipo;
import com.liga.model.Jugador;
import com.liga.model.Partido;

public class SimuladorService {

  private final SimuladorRapido simuladorRapido;
  private final SimuladorPro simuladorPro;

  public SimuladorService() {
    this.simuladorRapido = new SimuladorRapido();
    this.simuladorPro = new SimuladorPro();
  }

  public Partido jugarPartido(Partido partido, Jugador[] localXI, Jugador[] visitXI, String equipoUsuarioId) {

    boolean usuarioJuega = false;
    if (equipoUsuarioId != null) {
      String idLocal = partido.getEquipoLocal().getId();
      String idVisit = partido.getEquipoVisitante().getId();
      String userTeamCheck = equipoUsuarioId.trim();

      if (userTeamCheck != null && !userTeamCheck.isEmpty()) {
        // 1. Check by ID (Exact match)
        if (idLocal.trim().equalsIgnoreCase(userTeamCheck) || idVisit.trim().equalsIgnoreCase(userTeamCheck)) {
          usuarioJuega = true;
        }

        // 2. Check by Name (Strictly "FC Barcelona")
        if (!usuarioJuega) {
          String nombreLocal = partido.getEquipoLocal().getNombre();
          String nombreVisit = partido.getEquipoVisitante().getNombre();

          // STRICT CHECK: Exact match for "FC Barcelona"
          if (nombreLocal.equalsIgnoreCase("FC Barcelona") || nombreVisit.equalsIgnoreCase("FC Barcelona")) {
            usuarioJuega = true;
          } else if (nombreLocal.trim().equalsIgnoreCase(userTeamCheck)
              || nombreVisit.trim().equalsIgnoreCase(userTeamCheck)) {
            usuarioJuega = true;
          }
        }
      }
    }

    if (usuarioJuega) {
      simuladorPro.simularPartido(partido, localXI, visitXI);
    } else {
      simuladorRapido.simularPartido(partido, localXI, visitXI);
    }
    return partido;
  }
}
