package com.liga.controller;

import com.liga.model.*;
import com.liga.repository.LeagueRepository;
import com.liga.repository.LeagueRepositoryImpl;
import com.liga.repository.file.*;

import java.util.*;

public class EquipoController {

    private final Scanner sc = new Scanner(System.in);

    private final LeagueRepository repo =
            new LeagueRepositoryImpl(
                    new EquipoDAOImplJSON(), new JugadorDAOImplJSON(),
                    new MarketDAOImplJSON(), new UsersDAOImplJSON()
            );

    // 👉 NECESARIO PARA OTROS CONTROLADORES
    public LeagueRepository getRepo() {
        return repo;
    }

    public void menuEquipos() {
        System.out.println("\n=== EQUIPOS ===");

        List<Equipo> equipos = repo.listarEquipos();
        equipos.forEach(e -> System.out.println(e.getId() + " - " + e.getNombre()));

        System.out.print("Introduce ID del equipo para ver jugadores: ");
        String id = sc.nextLine();

        Optional<Equipo> eq = repo.buscarEquipoPorId(id);
        List<Jugador> jugadores = repo.buscarJugadorPorEquipo(id);

        if (eq.isEmpty()) {
            System.out.println("❌ Equipo no encontrado.");
            return;
        }

        System.out.println("\n--- Jugadores de " + eq.get().getNombre() + " ---");
        jugadores.forEach(j ->
                System.out.println(" - " + j.getNombre() + " (" + j.getPosicion() + ")")
        );
    }
}
