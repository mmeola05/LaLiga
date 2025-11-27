package com.liga.controller;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import com.liga.model.Equipo;
import com.liga.model.Jugador;
import com.liga.repository.LeagueRepository;
import com.liga.repository.LeagueRepositoryImpl;
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
    LeagueRepository leagueRepository = new LeagueRepositoryImpl(equipoDAO, jugadorDAO, marketDAO, usersDAO);

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
                case 0:
                    break;
                default:
                    System.out.println("Opcion no valida");
            }
        } while (opcion != 0);
    }
}
