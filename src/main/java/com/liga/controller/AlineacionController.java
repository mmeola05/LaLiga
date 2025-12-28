package com.liga.controller;

import com.liga.model.*;
import com.liga.repository.LeagueRepository;
import com.liga.repository.LeagueRepositoryImpl;
import com.liga.repository.file.*;

import java.util.*;

public class AlineacionController {

    private final Scanner sc = new Scanner(System.in);

    private final LeagueRepository repo =
            new LeagueRepositoryImpl(
                    new EquipoDAOImplJSON(), new JugadorDAOImplJSON(),
                    new MarketDAOImplJSON(), new UsersDAOImplJSON()
            );

    private final UsersDAOImplJSON usersDAO = new UsersDAOImplJSON();


    // ============================================================
    //   MOSTRAR EQUIPO
    // ============================================================
    public void mostrarEquipoUsuario(Usuario usuario) {

        if (usuario.getEquipo() == null) {
            System.out.println("No tienes equipo asignado.");
            return;
        }

        Optional<Equipo> equipo = repo.buscarEquipoPorId(usuario.getEquipo());

        if (equipo.isEmpty()) {
            System.out.println("Error: el equipo no existe.");
            return;
        }

        System.out.println("\n=== MI EQUIPO ===");
        System.out.println(equipo.get().getNombre());
        System.out.println("-----------------");
    }


    // ============================================================
    //   MOSTRAR ALINEACIÓN
    // ============================================================
    public void mostrarAlineacionUsuario(Usuario usuario) {
        if (usuario.getAlineacion() == null) {
            System.out.println("No tienes alineación definida.");
            return;
        }

        Alineacion al = usuario.getAlineacion();

        System.out.println("\n=== MI ALINEACIÓN ===");
        System.out.println("FORMACIÓN: " + al.getFormacion());

        System.out.print("DEL: ");
        System.out.println(String.join(" - ", al.getDelanteros()));

        System.out.print("MED: ");
        System.out.println(String.join(" - ", al.getMedios()));

        System.out.print("DEF: ");
        System.out.println(String.join(" - ", al.getDefensas()));

        System.out.println("POR: " + al.getPortero());
        System.out.println();
    }


    // ============================================================
    //   MOSTRAR PLANTILLA COMPLETA (titulares + suplentes)
    // ============================================================
    public void mostrarPlantilla(Usuario usuario) {

        if (usuario.getPlantilla() == null || usuario.getPlantilla().isEmpty()) {
            System.out.println("No tienes jugadores en la plantilla.");
            return;
        }

        Alineacion al = usuario.getAlineacion();
        Set<String> titulares = new HashSet<>(List.of(al.getPortero()));
        titulares.addAll(al.getDefensas());
        titulares.addAll(al.getMedios());
        titulares.addAll(al.getDelanteros());

        List<Jugador> jugadores = usuario.getPlantilla().stream()
                .map(id -> repo.buscarJugadorPorId(id).orElse(null))
                .filter(Objects::nonNull)
                .toList();

        System.out.println("\n=== PLANTILLA COMPLETA ===");

        System.out.println("\n--- TITULARES ---");
        jugadores.stream()
                .filter(j -> titulares.contains(j.getId()))
                .forEach(j -> System.out.println(j.getId() + " - " + j.getNombre() + " (" + j.getPosicion() + ")"));

        System.out.println("\n--- SUPLENTES ---");
        jugadores.stream()
                .filter(j -> !titulares.contains(j.getId()))
                .forEach(j -> System.out.println(j.getId() + " - " + j.getNombre() + " (" + j.getPosicion() + ")"));

        System.out.println("---------------------------\n");
    }


    // ============================================================
    //   EDITAR ALINEACIÓN
    // ============================================================
    public void editarAlineacion(Usuario usuario) {

        if (usuario.getAlineacion() == null) {
            System.out.println("No tienes alineación definida.");
            return;
        }

        int opcion;

        do {
            System.out.println("\n=== EDITAR ALINEACIÓN ===");
            mostrarAlineacionUsuario(usuario);
            System.out.println("1. Cambiar portero");
            System.out.println("2. Cambiar defensa");
            System.out.println("3. Cambiar medio");
            System.out.println("4. Cambiar delantero");
            System.out.println("0. Volver");

            opcion = sc.nextInt();
            sc.nextLine();

            switch (opcion) {
                case 1 -> cambiarJugadorEnLinea(usuario, "POR");
                case 2 -> cambiarJugadorEnLinea(usuario, "DEF");
                case 3 -> cambiarJugadorEnLinea(usuario, "MED");
                case 4 -> cambiarJugadorEnLinea(usuario, "DEL");
            }

        } while (opcion != 0);

        usersDAO.save(usuario);
        System.out.println("✔ Alineación actualizada.");
    }


    // ============================================================
    //   CAMBIAR UN JUGADOR DE UNA LÍNEA
    // ============================================================
    private void cambiarJugadorEnLinea(Usuario usuario, String linea) {

        Alineacion al = usuario.getAlineacion();

        // Lista actual de esa línea
        List<String> idsLineaActual =
                switch (linea) {
                    case "POR" -> List.of(al.getPortero());
                    case "DEF" -> new ArrayList<>(al.getDefensas());
                    case "MED" -> new ArrayList<>(al.getMedios());
                    case "DEL" -> new ArrayList<>(al.getDelanteros());
                    default -> null;
                };

        if (idsLineaActual == null) return;

        // Mostrar lista actual
        System.out.println("\nJugadores actuales en " + linea + ":");
        for (int i = 0; i < idsLineaActual.size(); i++) {
            Optional<Jugador> j = repo.buscarJugadorPorId(idsLineaActual.get(i));
            System.out.println((i + 1) + ". " + idsLineaActual.get(i) + " - "
                    + j.map(Jugador::getNombre).orElse("Desconocido"));
        }

        System.out.print("Elige índice a reemplazar: ");
        int index = sc.nextInt() - 1;
        sc.nextLine();

        if (index < 0 || index >= idsLineaActual.size()) {
            System.out.println("Índice no válido.");
            return;
        }

        String jugadorActual = idsLineaActual.get(index);

        // Cargar plantilla completa del usuario
        List<Jugador> jugadores =
                usuario.getPlantilla().stream()
                        .map(id -> repo.buscarJugadorPorId(id).orElse(null))
                        .filter(Objects::nonNull)
                        .toList();

        // Jugadores ya en alineación
        Set<String> ocupados = new HashSet<>();
        ocupados.add(al.getPortero());
        ocupados.addAll(al.getDefensas());
        ocupados.addAll(al.getMedios());
        ocupados.addAll(al.getDelanteros());

        // Filtrar candidatos
        Posicion posNecesaria = convertirLineaAPosicion(linea);

        List<Jugador> candidatos = jugadores.stream()
                .filter(j -> j.getPosicion() == posNecesaria)
                .filter(j -> !ocupados.contains(j.getId()) || j.getId().equals(jugadorActual))
                .toList();

        if (candidatos.isEmpty()) {
            System.out.println("No hay jugadores disponibles para esta posición.");
            return;
        }

        System.out.println("\n=== CANDIDATOS ===");
        candidatos.forEach(j ->
                System.out.println(j.getId() + " - " + j.getNombre() + " (" + j.getPosicion() + ")")
        );

        System.out.print("Escribe ID del nuevo jugador: ");
        String nuevoId = sc.nextLine();

        boolean valido = candidatos.stream().anyMatch(j -> j.getId().equals(nuevoId));

        if (!valido) {
            System.out.println("ID no válido.");
            return;
        }

        // Reemplazar
        switch (linea) {
            case "POR" -> al.setPortero(nuevoId);
            case "DEF" -> al.getDefensas().set(index, nuevoId);
            case "MED" -> al.getMedios().set(index, nuevoId);
            case "DEL" -> al.getDelanteros().set(index, nuevoId);
        }

        System.out.println("✔ Jugador reemplazado.");
    }


    private Posicion convertirLineaAPosicion(String linea) {
        return switch (linea) {
            case "POR" -> Posicion.PORTERO;
            case "DEF" -> Posicion.DEFENSA;
            case "MED" -> Posicion.MEDIO;
            case "DEL" -> Posicion.DELANTERO;
            default -> null;
        };
    }
}
