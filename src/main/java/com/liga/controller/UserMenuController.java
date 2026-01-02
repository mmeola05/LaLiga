package com.liga.controller;

import com.liga.model.*;
import com.liga.repository.LeagueRepository;
import com.liga.repository.LeagueRepositoryImpl;
import com.liga.repository.file.*;
import com.liga.service.UserService;
import com.liga.util.AlineacionGenerator;
import com.liga.repository.RepositoryFactory;
import java.util.*;
import com.liga.service.MarketService;

import com.liga.service.ClasificacionService;

public class UserMenuController {

    private final Scanner sc = new Scanner(System.in);

    private final UserService userService = new UserService();
    private final UsersDAOImplJSON usersDAO = new UsersDAOImplJSON();

    private final AlineacionController alineacionController = new AlineacionController();
    private final EquipoController equipoController = new EquipoController();

    private final LeagueRepository leagueRepository = RepositoryFactory.getLeagueRepository();

    private final MarketService marketService = new MarketService(RepositoryFactory.getLeagueRepository());

    private final ClasificacionService clasificacionService = new ClasificacionService();

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
                case 0 -> {
                }
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
            System.out.println("5. Mercado");
            System.out.println("6. Liga");
            System.out.println("0. Cerrar sesión");
            System.out.print("Opción: ");
            opcion = sc.nextInt();
            sc.nextLine();

            switch (opcion) {
                case 1 -> alineacionController.mostrarEquipoUsuario(usuario);
                case 2 -> alineacionController.mostrarAlineacionUsuario(usuario);
                case 3 -> alineacionController.editarAlineacion(usuario);
                case 4 -> alineacionController.mostrarPlantilla(usuario);
                case 5 -> menuMercado(usuario);
                case 6 -> menuLiga();
                case 0 -> System.out.println("Sesión cerrada.");
                default -> System.out.println("Opción no válida.");
            }

        } while (opcion != 0);
    }

    private void menuMercado(Usuario usuario) {

        int opcion;

        do {
            System.out.println("\n=== MERCADO DE JUGADORES ===");
            System.out.println("1. Ver jugadores en venta");
            System.out.println("2. Poner jugador en venta");
            System.out.println("3. Comprar jugador");
            System.out.println("0. Volver");
            System.out.print("Opción: ");

            opcion = sc.nextInt();
            sc.nextLine();

            switch (opcion) {
                case 1 -> mostrarMercado();
                case 2 -> ponerJugadorEnVenta(usuario);
                case 3 -> comprarJugadorMercado(usuario);
                case 0 -> System.out.println("Volviendo al menú anterior...");
                default -> System.out.println("Opción no válida.");
            }

        } while (opcion != 0);
    }

    // ============================================================
    // MENU LIGA
    // ============================================================
    private void menuLiga() {
        int opcion;
        do {
            System.out.println("\n=== LIGA ===");
            System.out.println("1. Ver clasificación");
            System.out.println("2. Simular jornada");
            System.out.println("3. Ver historial de jornadas");
            System.out.println("0. Volver");
            System.out.print("Opción: ");
            opcion = sc.nextInt();
            sc.nextLine();

            switch (opcion) {
                case 1 -> mostrarClasificacion();
                case 2 -> simularJornada();
                case 3 -> mostrarHistorialJornadas();
                case 0 -> {
                }
                default -> System.out.println("Opción no válida.");
            }
        } while (opcion != 0);
    }

    private void mostrarClasificacion() {
        // 1. Obtener todos los equipos
        List<Equipo> equipos = leagueRepository.listarEquipos();

        // 2. Cargar historial de partidos para calcular puntos actuales
        // NOTA: Como el atributo puntos se guarda en el equipo, si ya está actualizado
        // en el JSON
        // no hace falta recalcularlo. Pero si queremos asegurarnos de que está al día
        // con las jornadas
        // guardadas, podríamos re-procesar.
        // Por ahora asumimos que los equipos ya tienen sus estadisticas actualizadas
        // o llamamos a actualizar con todas las jornadas.

        List<Jornada> jornadas = leagueRepository.listarJornadas();
        // Resetear estadisticas antes de recalcular (opcional, pero recomendado si no
        // se guardan persistentes correctamente)
        // Como estamos guardando el objeto Equipo completo, las estadísticas deberían
        // estar ahí.
        // Pero para asegurar consistencia con las jornadas guardadas:
        for (Equipo e : equipos) {
            e.setPuntos(0);
            e.setPartidosJugados(0);
            e.setVictorias(0);
            e.setDerrotas(0);
            e.setEmpates(0);
            e.setGolesFavor(0);
            e.setGolesContra(0);
        }

        // Recalcular todo en base a jornadas jugadas
        for (Jornada j : jornadas) {
            clasificacionService.actualizarClasificacion(j.getPartidos());
            // IMPORTANTISIMO: actualizarClasificacion trabaja sobre las referencias de los
            // equipos en los partidos.
            // Necesitamos que esos partidos apunten a los objetos 'Equipo' de nuestra lista
            // 'equipos'.
            // Como los partidos se cargan del JSON, traen sus propias copias de Equipo.
            // Esto es un problema común. Vamos a hacer un apaño rápido: mapear los
            // resultados a nuestra lista 'equipos'.

            for (Partido p : j.getPartidos()) {
                Equipo local = buscarEnLista(equipos, p.getEquipoLocal().getId());
                Equipo visit = buscarEnLista(equipos, p.getEquipoVisitante().getId());

                if (local != null && visit != null) {
                    local.actualizarEstadisticas(p.getGolesLocal(), p.getGolesVisitante());
                    visit.actualizarEstadisticas(p.getGolesVisitante(), p.getGolesLocal());
                }
            }
        }

        // 3. Ordenar
        clasificacionService.ordenarClasificacion(equipos);

        // 4. Imprimir
        clasificacionService.imprimirClasificacion(equipos);
    }

    private Equipo buscarEnLista(List<Equipo> equipos, String id) {
        return equipos.stream().filter(e -> e.getId().equals(id)).findFirst().orElse(null);
    }

    private void mostrarHistorialJornadas() {
        List<Jornada> jornadas = leagueRepository.listarJornadas();

        if (jornadas.isEmpty()) {
            System.out.println("No hay jornadas jugadas aún.");
            return;
        }

        // Ordenar por número de jornada
        jornadas.sort(Comparator.comparingInt(Jornada::getNumJornada));

        for (Jornada jornada : jornadas) {
            System.out.println("\n========== JORNADA " + jornada.getNumJornada() + " ==========");
            for (Partido partido : jornada.getPartidos()) {
                System.out.printf("%-25s %d - %d %25s%n",
                        partido.getEquipoLocal().getNombre(),
                        partido.getGolesLocal(),
                        partido.getGolesVisitante(),
                        partido.getEquipoVisitante().getNombre());
            }
            System.out.println("-------------------------------------------------------");
        }
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

        Jornada jornadaSimulada = simulador.simularJornada(nextJornadaNumber, partidos);

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
                        .forEach(g -> System.out.printf("   Min %d' - %s (%s)%n",
                                g.getMinuto(),
                                g.getJugador().getNombre(),
                                g.getJugador().getEquipoId()
                                        .equals(p.getEquipoLocal().getId())
                                                ? "Local"
                                                : "Visitante"));
            }
            System.out.println("--------------------------------");
        }

        System.out.println("✔ Jornada simulada y guardada.\n");
    }

    public void iniciarApp() {
        menuUsuarios();
    }

    private void mostrarMercado() {

        List<JugadorMercado> mercado = marketService.listarMercado();

        if (mercado.isEmpty()) {
            System.out.println("No hay jugadores en el mercado.");
            return;
        }

        System.out.println("\n=== JUGADORES EN VENTA ===");

        for (JugadorMercado jm : mercado) {
            leagueRepository.buscarJugadorPorId(jm.getJugadorId())
                    .ifPresent(j -> System.out.printf(
                            "- ID Mercado: %s | %s (%s) | Precio: %.2f M | Vendedor: %s%n",
                            jm.getId(),
                            j.getNombre(),
                            j.getPosicion(),
                            jm.getPrecioSalida(),
                            jm.getVendedor()));
        }
    }

    private void ponerJugadorEnVenta(Usuario usuario) {

        if (usuario.getPlantilla() == null || usuario.getPlantilla().isEmpty()) {
            System.out.println("No tienes jugadores en tu plantilla.");
            return;
        }

        System.out.println("\n=== TU PLANTILLA ===");

        for (String jugadorId : usuario.getPlantilla()) {
            leagueRepository.buscarJugadorPorId(jugadorId)
                    .ifPresent(j -> System.out.printf(
                            "- %s | %s (%s)%n",
                            j.getId(),
                            j.getNombre(),
                            j.getPosicion()));
        }

        System.out.print("ID del jugador: ");
        String jugadorId = sc.nextLine();

        System.out.print("Precio de venta: ");
        double precio = sc.nextDouble();
        sc.nextLine();

        boolean ok = marketService.ponerEnVenta(
                usuario.getId(),
                jugadorId,
                precio);

        if (ok) {
            System.out.println("✔ Jugador puesto en venta correctamente.");
        } else {
            System.out.println("✘ No se pudo poner el jugador en venta.");
        }
    }

    private void comprarJugadorMercado(Usuario usuario) {

        System.out.println("\n=== COMPRAR JUGADOR ===");

        mostrarMercado();

        System.out.print("ID del jugador en mercado: ");
        String idMercado = sc.nextLine();

        boolean ok = marketService.comprarJugador(
                usuario.getId(),
                idMercado);

        if (ok) {
            System.out.println("✔ Compra realizada con éxito.");
        } else {
            System.out.println("✘ No se pudo realizar la compra.");
        }
    }

}
