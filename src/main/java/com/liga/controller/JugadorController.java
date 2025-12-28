package com.liga.controller;

import com.liga.model.*;
import com.liga.repository.LeagueRepository;
import com.liga.repository.LeagueRepositoryImpl;
import com.liga.repository.file.*;

import java.util.*;

public class JugadorController {

    private final Scanner sc = new Scanner(System.in);

    private final LeagueRepository repo =
            new LeagueRepositoryImpl(
                    new EquipoDAOImplJSON(), new JugadorDAOImplJSON(),
                    new MarketDAOImplJSON(), new UsersDAOImplJSON()
            );

    public void menuJugadores() {
        buscarJugadores();
    }

    public void buscarJugadores() {

        System.out.println("Buscar jugador por: ");
        System.out.println("1. Nombre");
        System.out.println("2. Posición");

        int opcion = sc.nextInt();
        sc.nextLine();

        System.out.print("Término: ");
        String t = sc.nextLine().toLowerCase();

        List<Jugador> jugadores = repo.listarJugadores();
        List<Jugador> filtrados;

        if (opcion == 1) {
            filtrados = jugadores.stream()
                    .filter(j -> j.getNombre().toLowerCase().contains(t))
                    .toList();
        } else {
            filtrados = jugadores.stream()
                    .filter(j -> j.getPosicion().name().toLowerCase().contains(t))
                    .toList();
        }

        System.out.println("\n=== RESULTADOS ===");
        filtrados.forEach(j -> {
            Optional<Equipo> eq = repo.buscarEquipoPorId(j.getEquipoId());
            System.out.println("- " + j.getNombre() + " (" + j.getPosicion() + ") - "
                    + eq.map(Equipo::getNombre).orElse("Desconocido"));
        });
    }
}
