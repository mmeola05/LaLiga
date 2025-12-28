package com.liga.controller;

import com.liga.model.*;
import com.liga.repository.file.UsersDAOImplJSON;
import com.liga.service.UserService;
import com.liga.util.AlineacionGenerator;

import java.util.*;

public class UserMenuController {

    private final Scanner sc = new Scanner(System.in);

    private final UserService userService = new UserService();
    private final UsersDAOImplJSON usersDAO = new UsersDAOImplJSON();

    private final AlineacionController alineacionController = new AlineacionController();
    private final EquipoController equipoController = new EquipoController();

    public void menuUsuarios() {
        int opcion;

        do {
            System.out.println("\n=== GESTIÓN DE USUARIOS ===");
            System.out.println("1. Iniciar sesión");
            System.out.println("2. Registrarse");
            System.out.println("0. Volver");
            System.out.print("Opción: ");
            opcion = sc.nextInt();
            sc.nextLine();

            switch (opcion) {
                case 1 -> iniciarSesion();
                case 2 -> registrarUsuario();
                case 0 -> {}
                default -> System.out.println("Opción no válida.");
            }

        } while (opcion != 0);
    }


    private void iniciarSesion() {
        System.out.print("Email: ");
        String email = sc.nextLine();

        System.out.print("Password: ");
        String pass = sc.nextLine();

        Usuario u = userService.login(email, pass);

        if (u != null) {
            System.out.println("Bienvenido " + u.getEmail());
            menuUsuarioLogueado(u);
        } else {
            System.out.println("Credenciales incorrectas.");
        }
    }

    private void registrarUsuario() {
        System.out.print("Email: ");
        String email = sc.nextLine();

        System.out.print("Password: ");
        String password = sc.nextLine();

        // Equipos disponibles
        List<String> ocupados = usersDAO.findAll().stream()
                .map(Usuario::getEquipo)
                .filter(Objects::nonNull)
                .toList();

        List<Equipo> disponibles = equipoController.getRepo().listarEquipos()
                .stream()
                .filter(e -> !ocupados.contains(e.getId()))
                .toList();

        System.out.println("\n=== EQUIPOS DISPONIBLES ===");
        disponibles.forEach(eq -> System.out.println(eq.getId() + " - " + eq.getNombre()));

        System.out.print("Elige equipo: ");
        String equipoId = sc.nextLine();

        // Obtener jugadores del equipo
        List<Jugador> jugadores = equipoController.getRepo().buscarJugadorPorEquipo(equipoId);

        Alineacion al = AlineacionGenerator.generar(jugadores);
        List<String> plantilla = jugadores.stream().map(Jugador::getId).toList();

        Usuario nuevo = userService.registrar(email, password, equipoId, al, plantilla);

        if (nuevo != null)
            System.out.println("✔ Usuario registrado con ID: " + nuevo.getId());
        else
            System.out.println("✘ Ese email ya está registrado.");
    }


    private void menuUsuarioLogueado(Usuario usuario) {
        int opcion;

        do {
            System.out.println("\n=== MI PERFIL ===");
            System.out.println("1. Ver equipo");
            System.out.println("2. Ver alineación");
            System.out.println("3. Editar alineación");
            System.out.println("4. Ver plantilla (banquillo)");
            System.out.println("0. Cerrar sesión");
            System.out.print("Opción: ");
            opcion = sc.nextInt();
            sc.nextLine();

            switch (opcion) {
                case 1 -> alineacionController.mostrarEquipoUsuario(usuario);
                case 2 -> alineacionController.mostrarAlineacionUsuario(usuario);
                case 3 -> alineacionController.editarAlineacion(usuario);
                case 4 -> alineacionController.mostrarPlantilla(usuario);
                case 0 -> System.out.println("Sesión cerrada.");
                default -> System.out.println("Opción no válida.");
            }

        } while (opcion != 0);
    }
    public void iniciarApp() {
        menuUsuarios();
    }

}

