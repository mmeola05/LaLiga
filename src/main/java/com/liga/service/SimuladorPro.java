package com.liga.service;

import com.liga.model.Equipo;
import com.liga.model.Jugador;
import com.liga.model.Partido;

import java.util.Random;

public class SimuladorPro {

  private final Random random = new Random();

  public void simularPartido(Partido partido, Jugador[] jugadoresLocal, Jugador[] jugadoresVisitante) {
    Equipo local = partido.getEquipoLocal();
    Equipo visitante = partido.getEquipoVisitante();
    boolean posesionLocal = true; // Empieza local por defecto o random
    int posicionBalon = 5; // Medio campo
    int rachaVictorias = 0; // Para factor fatiga
    Jugador tirador = null; // Para guardar quién iba a tirar en pos 10

    // Solo narrar inicio
    // narrar("=== INICIO DEL PARTIDO ===");
    // narrar(local.getNombre() + " vs " + visitante.getNombre());

    double momentum = 0.0; // Bonus acumulativo por posesión mantenida

    for (int minuto = 1; minuto <= 90; minuto++) {

      if (minuto == 46) {
        narrar("--- DESCANSO ---");
      }

      // EVENTO ALEATORIO: Falta o Balón Fuera (Pérdida de tiempo)
      if (random.nextDouble() < 0.03) { // 3% probabilidad
        if (random.nextBoolean()) {
          // narrar("Min " + minuto + ": Balón fuera. Saque de banda.");
        } else {
          // narrar("Min " + minuto + ": Falta en el medio campo. El juego se detiene.");
        }
        continue; // Pasa el minuto sin avance
      }

      try {
        Thread.sleep(200); // Retardo para efecto 'streaming'
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }

      Jugador atacante;
      Jugador defensor;
      Equipo equipoAtacante = posesionLocal ? local : visitante;

      // Determinar jugadores
      // Determinar jugadores - SELECCION VARIADA
      if (posesionLocal) {
        atacante = seleccionarJugador(jugadoresLocal, posicionBalon, true);
        defensor = seleccionarJugador(jugadoresVisitante, 10 - posicionBalon, false);
      } else {
        atacante = seleccionarJugador(jugadoresVisitante, posicionBalon, true);
        defensor = seleccionarJugador(jugadoresLocal, 10 - posicionBalon, false);
      }

      if (atacante == null || defensor == null)
        continue;

      // Narración base
      // narrar("Min " + minuto + ": " + atacante.getNombre() + " tiene el balón en la
      // zona " + posicionBalon);

      // Resolver Duelo
      // Stat atacante vs Stat defensor
      // Si posBalon == 10 (Delantero vs Portero) -> Ataque vs Porteria

      double statAtacante = obtenerStatRelevante(atacante, true, posicionBalon);
      double statDefensor = obtenerStatRelevante(defensor, false, 10 - posicionBalon);

      // Fórmula: stat + random(-5, 5) -> AHORA (-30, 30) para más variedad
      // FATIGA: Si rachaVictorias >= 3, penalización
      double fatiga = (rachaVictorias >= 3) ? -15.0 : 0.0;

      // BONIFICACIÓN TIRO: Si está en zona 10, el atacante tiene ventaja para premiar
      // la llegada.
      double bonusAtaque = (posicionBalon == 10) ? 20.0 : 2.0; // +2 base por iniciativa

      // Random más amplio (-30 a 30) para permitir sorpresas y que no gane siempre el
      // mismo
      double factorSuerteA = (random.nextDouble() * 60 - 30);
      double factorSuerteD = (random.nextDouble() * 60 - 30);

      double valorA = statAtacante + bonusAtaque + momentum + fatiga + factorSuerteA;
      double valorD = statDefensor + factorSuerteD;

      // Logica de PASE SEGURO / MANTENER POSESION si la diferencia es pequeña
      // Si atacante pierde por poco (ej. < 10 puntos), no pierde balón, mantiene
      // posesión.
      boolean mantienePosesion = false;
      if (valorD > valorA && (valorD - valorA) < 10.0 && posicionBalon >= 4 && posicionBalon <= 8) {
        mantienePosesion = true;
        // No avanza, pero no la pierde.
        // Reset momentum parcial
        momentum = Math.max(0, momentum - 2.0);
      }

      if (valorA > valorD || mantienePosesion) {

        if (mantienePosesion) {
          if (random.nextDouble() < 0.15) {
            narrar("Min " + minuto + ": " + atacante.getNombre() + " toca en corto y mantiene la posesión.");
          }
          continue; // Pasa minuto, misma posición, misma posesión
        }

        // GANA ATACANTE
        posicionBalon++;
        momentum += 5.0; // Gana confianza con cada avance
        rachaVictorias++;

        // SOLO IMPRIMIR SI ES EVENTO IMPORTANTE O GOL
        // narrar("Min " + minuto + ": " + atacante.getNombre() + " regatea a " +
        // defensor.getNombre() + " y avanza."); // REMOVED

        if (posicionBalon == 11) {
          // GOL - EL QUE LLEGÓ A 11 ES EL QUE TIRÓ DESDE 10
          if (tirador == null)
            tirador = atacante; // Fallback
          narrar("¡¡¡GOOOOOL de " + tirador.getNombre() + "!!! (" + equipoAtacante.getNombre() + ")");

          // PERSISTENCIA DEL GOL
          partido.addGol(new com.liga.model.Gol(tirador, minuto));

          tirador = null; // Reset tirador

          if (posesionLocal)
            partido.setGolesLocal(partido.getGolesLocal() + 1);
          else
            partido.setGolesVisitante(partido.getGolesVisitante() + 1);

          narrar("Min " + minuto + " | Marcador: " + local.getNombre() + " " + partido.getGolesLocal() + " - "
              + partido.getGolesVisitante() + " " + visitante.getNombre());

          // Reset
          posicionBalon = 5;
          posesionLocal = !posesionLocal;
          momentum = 0.0; // Reset momentum
          rachaVictorias = 0;
        } else if (posicionBalon == 10) {
          // Change narration to imply shooting imminent
          tirador = atacante; // Guardamos el tirador
          narrar("Min " + minuto + ": ¡" + atacante.getNombre() + " se prepara para disparar!");
        } else if (posicionBalon >= 8) {
          narrar("Min " + minuto + ": " + atacante.getNombre() + " genera peligro en el área.");
        } else {
          // Silencio en medio campo salvo muy rara vez o fatiga
          if (fatiga < 0) {
            // narrar("Min " + minuto + ": " + atacante.getNombre() + " parece cansado pero
            // sigue avanzando.");
          }
        }
      } else {
        // GANA DEFENSOR (Robo)
        rachaVictorias = 0; // Reset racha atacante

        if (posicionBalon >= 9) {
          narrar("Min " + minuto + ": ¡Paradón/Corte providencial de " + defensor.getNombre() + "!");
          // Lógica de rechace / corner
          if (random.nextBoolean()) {
            // CORNER
            narrar("Min " + minuto + ": ¡El balón se va a córner!");
            posicionBalon = 8; // Cerca del área
            // La posesión NO cambia (sigue atacando)
            // momentum reduce un poco
            momentum = Math.max(0, momentum - 5.0);
            continue; // Salto al siguiente minuto con misma posesión en zona 8
          } else {
            // DESPEJE
            narrar("Min " + minuto + ": La defensa despeja el peligro.");
            posesionLocal = !posesionLocal; // Cambio posesion
            posicionBalon = 5; // Balón al medio
            momentum = 0.0;
            continue;
          }
        } else {
          // Solo narrar robo si es cambio de posesión (siempre lo es aquí) pero evitamos
          // spam en medio campo
          if (minuto % 5 == 0 || random.nextDouble() < 0.2) {
            narrar("Min " + minuto + ": " + defensor.getNombre() + " roba el balón.");
          }
        }

        posesionLocal = !posesionLocal;
        // LÓGICA DE REBOTE: No invertir a la misma posición espejo siempre.
        // Si pierdo en 5 (Medio), el rival recupera en 5 (Medio). -> 10-5 = 5. Bucle.
        // SOLUCIÓN: El que recupera "retrocede" un poco para "asegurar" el balón.
        // Si recupero en mi campo (pos 0-4 rival -> pos 10-6 mio), estoy arriba.
        // Si recupero en mi area (pos 9-10 rival -> pos 1-0 mio).

        int posRecuperacion = 10 - posicionBalon;

        // CORRECCIÓN ANTI-BUCLE: Si la recuperación es en el medio (5), forzar
        // movimiento.
        // Lo mandamos a 4 (defensa) para que tenga que construir, o 6 (ataque) si es
        // contra.
        // Vamos a hacer que retroceda un paso para "asegurar" el balón.
        if (posRecuperacion == 5) {
          posRecuperacion = 4;
        }

        posicionBalon = posRecuperacion;
        momentum = 0.0; // Reset momentum
      }
    }

    narrar("=== FIN DEL PARTIDO ===");
    narrar("Resultado Final: " + local.getNombre() + " " + partido.getGolesLocal() + " - " + partido.getGolesVisitante()
        + " " + visitante.getNombre());

    // Actualizar stats finales
    local.actualizarEstadisticas(partido.getGolesLocal(), partido.getGolesVisitante());
    visitante.actualizarEstadisticas(partido.getGolesVisitante(), partido.getGolesLocal());

  }

  private double obtenerStatRelevante(Jugador j, boolean esAtacante, int posicionEnCampo) {
    if (j == null)
      return 50.0;

    // Si es atacante y está en zona 10 (tiro), usa Ataque.
    // Si es defensor y está en zona 0 (portería), usa Porteria.
    if (esAtacante) {
      if (posicionEnCampo >= 10)
        return j.getAtaque(); // Tiro
      if (posicionEnCampo >= 6)
        return j.getAtaque(); // Ofensivo
      return j.getPase(); // Medio campo / salida
    } else {
      // Defensor
      if (posicionEnCampo == 0)
        return j.getPorteria(); // Portero parando
      return j.getDefensa(); // Robo
    }
  }

  // MÉTODO NUEVO: Selección inteligente y variada de jugadores
  private Jugador seleccionarJugador(Jugador[] alineacion, int zona, boolean esAtacante) {
    // Zona 0-3: Defensas (indices 1-4 aprox) + Portero (0)
    // Zona 4-7: Medios (indices 5-8 aprox)
    // Zona 8-10: Delanteros (indices 9-10 aprox)

    // Alineación Array size 11.
    // [0]=Port, [1-4]=Def, [5-8]=Med, [9-10]=Del

    int minIdx = 0;
    int maxIdx = 10;

    if (zona <= 3) { // Zona defensiva
      minIdx = 0;
      maxIdx = 4;
    } else if (zona <= 7) { // Medio campo
      minIdx = 4;
      maxIdx = 8;
    } else { // Zona de ataque
      minIdx = 7;
      maxIdx = 10;
      if (esAtacante) { // Si atacas en zona final, prioriza delanteros
        minIdx = 9;
      }
    }

    // Intentar buscar jugador válido en ese rango
    java.util.List<Jugador> opciones = new java.util.ArrayList<>();
    for (int i = minIdx; i <= maxIdx; i++) {
      if (i >= 0 && i < alineacion.length && alineacion[i] != null) {
        opciones.add(alineacion[i]);
      }
    }

    // Si no hay nadie específico (raro), ampliar rango a todo el equipo
    if (opciones.isEmpty()) {
      for (Jugador j : alineacion) {
        if (j != null)
          opciones.add(j);
      }
    }

    // Devolver uno random de las opciones
    if (opciones.isEmpty())
      return null;
    return opciones.get(random.nextInt(opciones.size()));
  }

  private void narrar(String mensaje) {
    System.out.println(mensaje);
  }
}
