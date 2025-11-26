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

    // TODO: aquí se controla todo sobre el menu, luego se llamará a esta clase
    // desde el main. Poner scanner
    // y luego llamar a funciones de menu principal en esta clase

    public void ejecutarMenu() {
        int opcion;
        do {
            menuPrincipal.mostrarMenu();
            opcion = sc.nextInt();
            switch (opcion) {
                case 1:
                    break;
                case 2:
                    System.out.println("Escribe el id del equipo");
                    String idEquipo = sc.nextLine();
                    try {
                        List<Jugador> jugadores = leagueRepository.buscarJugadorPorEquipo(idEquipo);
                        System.out.println(jugadores);
                    } catch (Exception e) {
                        System.out.println("Equipo no encontrado");
                    }
                    break;
                case 3:
                    break;
                case 4:
                    break;
                case 5:
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opcion no valida");
            }
        } while (opcion != 0);
    }
}
