package com.liga.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Scanner;

import com.liga.model.Equipo;
import com.liga.model.Gol;
import com.liga.model.Jornada;
import com.liga.model.Jugador;
import com.liga.model.Partido;
import com.liga.repository.LeagueRepository;
import com.liga.repository.LeagueRepositoryImpl;
import com.liga.repository.RepositoryFactory;
import com.liga.repository.dao.EquipoDAO;
import com.liga.repository.dao.JugadorDAO;
import com.liga.repository.dao.MarketDAO;
import com.liga.repository.dao.UsersDAO;
import com.liga.repository.file.EquipoDAOImplJSON;
import com.liga.repository.file.JugadorDAOImplJSON;
import com.liga.repository.file.MarketDAOImplJSON;
import com.liga.repository.file.UsersDAOImplJSON;
import com.liga.view.cli.MenuPrincipal;

public class VistaPrincipalController {
    Scanner sc = new Scanner(System.in);
    EquipoDAO equipoDAO = new EquipoDAOImplJSON();
    JugadorDAO jugadorDAO = new JugadorDAOImplJSON();
    MarketDAO marketDAO = new MarketDAOImplJSON();
    UsersDAO usersDAO = new UsersDAOImplJSON();
    LeagueRepository leagueRepository = RepositoryFactory.getLeagueRepository();

    MenuPrincipal menuPrincipal;

    public VistaPrincipalController(MenuPrincipal menuPrincipal) {
        this.menuPrincipal = menuPrincipal;
    }

    public void ejecutarMenu() {
        int opcion;
        do {
            menuPrincipal.mostrarMenu();
            opcion = sc.nextInt();
            sc.nextLine();
            switch (opcion) {
                case 1:
                    break;
                case 2:
                    try {
                        System.out.println("Equipos Disponibles: ");
                        List<Equipo> equipos = leagueRepository.listarEquipos();
                        if (equipos.isEmpty()) {
                            System.out.println("No se encontraron equipos");
                            break;
                        } else {
                            for (Equipo equipo : equipos) {
                                System.out.println(equipo.getId() + " - " + equipo.getNombre());
                            }
                        }
                        System.out.print("Escribe el id del equipo: ");
                        String idEquipo = sc.nextLine();
                        Optional<Equipo> equipo = leagueRepository.buscarEquipoPorId(idEquipo);
                        List<Jugador> jugadores = leagueRepository.buscarJugadorPorEquipo(idEquipo);
                        if (jugadores.isEmpty()) {
                            System.out.println("No se encontraron jugadores para el equipo con ID '" + idEquipo
                                    + "' o el equipo no existe.");
                        } else {
                            System.out.println("\n--- Jugadores del " + equipo.get().getNombre() + " ---");
                            for (Jugador jugador : jugadores) {
                                System.out.println(" - " + jugador.getNombre() + " (" + jugador.getPosicion() + ")");
                            }
                            System.out.println("----------------------------\n");
                        }
                    } catch (Exception e) {
                        System.out.println("Equipo no encontrado");
                    }
                    break;
                case 3:
                    System.out.println("Buscar jugador por: ");
                    System.out.println("1. Nombre");
                    System.out.println("2. Posición");
                    System.out.print("Elige una opción: ");
                    int opcionBuscarJugador = sc.nextInt();
                    sc.nextLine();

                    System.out.print("Introduce el término de búsqueda: ");
                    String searchTerm = sc.nextLine().toLowerCase();

                    List<Jugador> jugadores = leagueRepository.listarJugadores();
                    List<Jugador> jugadoresFiltrados;

                    if (opcionBuscarJugador == 1) {
                        jugadoresFiltrados = jugadores.stream()
                                .filter(j -> j.getNombre().toLowerCase().contains(searchTerm))
                                .toList();
                    } else {
                        jugadoresFiltrados = jugadores.stream()
                                .filter(j -> j.getPosicion().name().toLowerCase().contains(searchTerm))
                                .toList();
                    }

                    if (jugadoresFiltrados.isEmpty()) {
                        System.out.println("No se encontraron jugadores con el término de búsqueda: " + searchTerm);
                    } else {
                        System.out.println("\n--- Jugadores encontrados ---");
                        for (Jugador jugador : jugadoresFiltrados) {
                            Optional<Equipo> equipo = leagueRepository.buscarEquipoPorId(jugador.getEquipoId());
                            String nombreEquipo = equipo.map(Equipo::getNombre).orElse("Desconocido");
                            System.out.println(
                                    "- " + jugador.getNombre() + " (" + jugador.getPosicion() + ") - " + nombreEquipo);
                        }
                        System.out.println("---------------------------\n");
                    }
                    break;
                case 4:
                    break;
                case 5:
                    break;
                case 6:
                    break;
                case 7:
                    System.out.println("Simulando jornada...");
                    SimuladorJornada simulador = new SimuladorJornada();
                    int nextJornadaNumber = 1;
                    Optional<Jornada> lastJornada = leagueRepository.listarJornadas().stream()
                            .max((j1, j2) -> Integer.compare(j1.getNumJornada(), j2.getNumJornada()));
                    if (lastJornada.isPresent()) {
                        nextJornadaNumber = lastJornada.get().getNumJornada() + 1;
                    }

                    List<Equipo> allEquipos = leagueRepository.listarEquipos();
                    if (allEquipos.size() < 2) {
                        System.out.println("No hay suficientes equipos para simular una jornada");
                        break;
                    }

                    List<Partido> partidosSimular = new ArrayList<>();

                    Collections.shuffle(allEquipos);
                    for (int i = 0; i < allEquipos.size(); i += 2) {
                        Equipo equipoLocal = allEquipos.get(i);
                        Equipo equipoVisitante = allEquipos.get(i + 1);
                        partidosSimular.add(new Partido(equipoLocal, equipoVisitante));
                    }

                    Jornada jorandaSimulda = simulador.simularJornada(nextJornadaNumber, partidosSimular);

                    leagueRepository.guardarJornada(jorandaSimulda);

                    System.out.println("\n========================================");
                    System.out.println("       RESULTADOS JORNADA " + jorandaSimulda.getNumJornada());
                    System.out.println("========================================\n");

                    for (Partido partido : jorandaSimulda.getPartidos()) {
                        System.out.printf("%s [%d - %d] %s%n",
                                partido.getEquipoLocal().getNombre(),
                                partido.getGolesLocal(),
                                partido.getGolesVisitante(),
                                partido.getEquipoVisitante().getNombre());

                        List<Gol> goles = partido.getGoles();
                        if (!goles.isEmpty()) {
                            goles.sort((g1, g2) -> Integer.compare(g1.getMinuto(), g2.getMinuto()));
                            for (Gol gol : goles) {
                                System.out.printf("   Min %d' - %s (%s)%n",
                                        gol.getMinuto(),
                                        gol.getJugador().getNombre(),
                                        gol.getJugador().getEquipoId().equals(partido.getEquipoLocal().getId())
                                                ? "Local"
                                                : "Visitante");
                            }
                        }
                        System.out.println("----------------------------------------");
                    }
                    System.out.println("Jornada simulada y guardada exitosamente.\n");
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opcion no valida");
            }
        } while (opcion != 0);
    }
}
