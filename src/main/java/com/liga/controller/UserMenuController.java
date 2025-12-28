package com.liga.controller;

import com.liga.model.*;
import com.liga.repository.LeagueRepository;
import com.liga.repository.LeagueRepositoryImpl;
import com.liga.repository.file.*;
import com.liga.service.UserService;
import com.liga.util.AlineacionGenerator;

import java.util.*;

public class UserMenuController {

    private final Scanner sc = new Scanner(System.in);

    private final UserService userService = new UserService();
    private final UsersDAOImplJSON usersDAO = new UsersDAOImplJSON();

    private final AlineacionController alineacionController = new AlineacionController();
    private final EquipoController equipoController = new EquipoController();

    private final LeagueRepository leagueRepository =
            new LeagueRepositoryImpl(
                    new EquipoDAOImplJSON(),
                    new JugadorDAOImplJSON(),
                    new MarketDAOImplJSON(),
                    new UsersDAOImplJSON(),
                    new JornadaDAOImplJSON()
            );

    // ============================================================
    // MENÚ PRINCIPAL
    // ============================================================
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

    // ============================================================
    // LOGIN
    // ============================================================
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

    // ============================================================
    // REGISTRO
    // ============================================================
    private void registrarUsuario() {
        System.out.print("Email: ");
        String email = sc.nextLine();

        System.out.print("Password: ");
        String password = sc.nextLine();

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

        List<Jugador> jugadores = equipoController.getRepo().buscarJugadorPorEquipo(equipoId);

        Alineacion al = AlineacionGenerator.generar(jugadores);
        List<String> plantilla = jugadores.stream().map(Jugador::getId).toList();

        Usuario nuevo = userService.registrar(email, password, equipoId, al, plantilla);

        if (nuevo != null)
            System.out.println("✔ Usuario registrado con ID: " + nuevo.getId());
        else
            System.out.println("✘ Ese email ya está registrado.");
    }

    // ============================================================
    // MENÚ USUARIO LOGUEADO
    // ============================================================
    private void menuUsuarioLogueado(Usuario usuario) {
        int opcion;

        do {
            System.out.println("\n=== MI PERFIL ===");
            System.out.println("1. Ver equipo");
            System.out.println("2. Ver alineación");
            System.out.println("3. Editar alineación");
            System.out.println("4. Ver plantilla (banquillo)");
            System.out.println("7. Simular jornada");
            System.out.println("0. Cerrar sesión");
            System.out.print("Opción: ");
            opcion = sc.nextInt();
            sc.nextLine();

            switch (opcion) {
                case 1 -> alineacionController.mostrarEquipoUsuario(usuario);
                case 2 -> alineacionController.mostrarAlineacionUsuario(usuario);
                case 3 -> alineacionController.editarAlineacion(usuario);
                case 4 -> alineacionController.mostrarPlantilla(usuario);
                case 7 -> simularJornada();
                case 0 -> System.out.println("Sesión cerrada.");
                default -> System.out.println("Opción no válida.");
            }

        } while (opcion != 0);
    }

    // ============================================================
    // SIMULAR JORNADA
    // ============================================================
    private void simularJornada() {

        System.out.println("Simulando jornada...");

        SimuladorJornada simulador = new SimuladorJornada();

        int nextJornadaNumber = leagueRepository.listarJornadas().stream()
                .mapToInt(Jornada::getNumJornada)
                .max()
                .orElse(0) + 1;

        List<Equipo> allEquipos = new ArrayList<>(leagueRepository.listarEquipos());

        if (allEquipos.size() < 2 || allEquipos.size() % 2 != 0) {
            System.out.println("No hay suficientes equipos (o son impares).");
            return;
        }

        Collections.shuffle(allEquipos);

        List<Partido> partidos = new ArrayList<>();

        for (int i = 0; i < allEquipos.size(); i += 2) {
            partidos.add(new Partido(allEquipos.get(i), allEquipos.get(i + 1)));
        }

        Jornada jornadaSimulada =
                simulador.simularJornada(nextJornadaNumber, partidos);

        leagueRepository.guardarJornada(jornadaSimulada);

        System.out.println("\n=== RESULTADOS JORNADA " + jornadaSimulada.getNumJornada() + " ===");

        for (Partido p : jornadaSimulada.getPartidos()) {
            System.out.printf("%s [%d - %d] %s%n",
                    p.getEquipoLocal().getNombre(),
                    p.getGolesLocal(),
                    p.getGolesVisitante(),
                    p.getEquipoVisitante().getNombre());

            if (p.getGoles() != null) {
                p.getGoles().stream()
                        .sorted(Comparator.comparingInt(Gol::getMinuto))
                        .forEach(g ->
                                System.out.printf("   Min %d' - %s (%s)%n",
                                        g.getMinuto(),
                                        g.getJugador().getNombre(),
                                        g.getJugador().getEquipoId()
                                                .equals(p.getEquipoLocal().getId())
                                                ? "Local" : "Visitante")
                        );
            }
            System.out.println("--------------------------------");
        }

        System.out.println("✔ Jornada simulada y guardada.\n");
    }

    public void iniciarApp() {
        menuUsuarios();
    }
}
