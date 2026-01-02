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
                case 6 -> menuLiga(usuario);
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
    // ============================================================
    // MENU LIGA
    // ============================================================
    private void menuLiga(Usuario usuario) {
        int opcion;
        do {
            System.out.println("\n=== LIGA ===");
            System.out.println("1. Ver clasificación");
            System.out.println("2. Ver goleadores");
            System.out.println("3. Simular jornada");
            System.out.println("4. Ver historial de jornadas");
            System.out.println("0. Volver");
            System.out.print("Opción: ");
            opcion = sc.nextInt();
            sc.nextLine();

            switch (opcion) {
                case 1 -> mostrarClasificacion(usuario);
                case 2 -> mostrarGoleadores(usuario);
                case 3 -> simularJornada(usuario);
                case 4 -> mostrarHistorialJornadas(usuario);
                case 0 -> {
                }
                default -> System.out.println("Opción no válida.");
            }
        } while (opcion != 0);
    }

    private void mostrarClasificacion(Usuario usuario) {
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
        String userTeamId = (usuario != null) ? usuario.getEquipo() : null;
        clasificacionService.imprimirClasificacion(equipos, userTeamId);
    }

    // ============================================================
    // MOSTRAR GOLEADORES
    // ============================================================
    private void mostrarGoleadores(Usuario usuario) {
        // Mapa para contar goles: Jugador -> Integer
        Map<String, Integer> tablaGoleadores = new HashMap<>();
        Map<String, Jugador> infoJugadores = new HashMap<>();

        // 1. Recorrer historial de partidos
        List<Jornada> jornadas = leagueRepository.listarJornadas();

        if (jornadas.isEmpty()) {
            System.out.println("No hay goles para mostrar (sin jornadas jugadas).");
            return;
        }

        for (Jornada j : jornadas) {
            for (Partido p : j.getPartidos()) {
                if (p.getGoles() != null) {
                    for (Gol gol : p.getGoles()) {
                        String idJugador = gol.getJugador().getId();

                        // Sumar gol
                        tablaGoleadores.put(idJugador, tablaGoleadores.getOrDefault(idJugador, 0) + 1);

                        // Guardar referencia al jugador para tener nombre, equipo...
                        if (!infoJugadores.containsKey(idJugador)) {
                            infoJugadores.put(idJugador, gol.getJugador());
                        }
                    }
                }
            }
        }

        if (tablaGoleadores.isEmpty()) {
            System.out.println("Aún no se han marcado goles.");
            return;
        }

        // 2. Ordenar por goles (descendente)
        List<Map.Entry<String, Integer>> listaOrdenada = new ArrayList<>(tablaGoleadores.entrySet());
        listaOrdenada.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        // 3. Mostrar Top 20 (o todos)
        System.out.println("\n========== TABLA DE GOLEADORES ==========");
        System.out.printf("%-5s %-30s %-25s %s%n", "Pos", "Jugador", "Equipo", "Goles");
        System.out.println("-------------------------------------------------------------------------");

        // ANSI Colors
        final String GREEN_BOLD = "\u001B[1;32m";
        final String RESET = "\u001B[0m";
        String userTeamId = (usuario != null) ? usuario.getEquipo() : null;

        int pos = 1;
        int limit = 20; // Limite para no spamear la terminal

        for (Map.Entry<String, Integer> entry : listaOrdenada) {
            if (pos > limit)
                break;

            String id = entry.getKey();
            int goles = entry.getValue();
            Jugador j = infoJugadores.get(id);

            // Intentar obtener el nombre del equipo actual del jugador
            Optional<Equipo> equipoOpt = leagueRepository.buscarEquipoPorId(j.getEquipoId());
            String nombreEquipo = equipoOpt.map(Equipo::getNombre).orElse("Sin Equipo");
            String equipoId = j.getEquipoId();

            boolean isUserPlayer = userTeamId != null && userTeamId.equals(equipoId);

            if (isUserPlayer)
                System.out.print(GREEN_BOLD);

            System.out.printf("%-5d %-30s %-25s %d%n",
                    pos++,
                    j.getNombre(),
                    nombreEquipo + (isUserPlayer ? " *" : ""),
                    goles);

            if (isUserPlayer)
                System.out.print(RESET);
        }
        System.out.println("=========================================================================\n");
    }

    private Equipo buscarEnLista(List<Equipo> equipos, String id) {
        return equipos.stream().filter(e -> e.getId().equals(id)).findFirst().orElse(null);
    }

    private void mostrarHistorialJornadas(Usuario usuario) {
        List<Jornada> jornadas = leagueRepository.listarJornadas();

        if (jornadas.isEmpty()) {
            System.out.println("No hay jornadas jugadas aún.");
            return;
        }

        final String GREEN_BOLD = "\u001B[1;32m";
        final String RESET = "\u001B[0m";
        String userTeamId = (usuario != null) ? usuario.getEquipo() : null;

        // Ordenar por número de jornada
        jornadas.sort(Comparator.comparingInt(Jornada::getNumJornada));

        for (Jornada jornada : jornadas) {
            System.out.println("\n========== JORNADA " + jornada.getNumJornada() + " ==========");
            for (Partido partido : jornada.getPartidos()) {

                boolean localIsUser = userTeamId != null && userTeamId.equals(partido.getEquipoLocal().getId());
                boolean visitIsUser = userTeamId != null && userTeamId.equals(partido.getEquipoVisitante().getId());

                String nombreLocalOriginal = partido.getEquipoLocal().getNombre();
                String nombreVisitOriginal = partido.getEquipoVisitante().getNombre();

                // Añadimos el asterisco si es necesario para calcular la longitud VISUAL
                String visualLocal = localIsUser ? "* " + nombreLocalOriginal : nombreLocalOriginal;
                String visualVisit = visitIsUser ? "* " + nombreVisitOriginal : nombreVisitOriginal;

                // Definimos ancho de columna
                int colWidth = 25;

                // 1. Imprimir Local (Alineado a la IZQUIERDA)
                // Imprimimos el texto con color si hace falta
                if (localIsUser)
                    System.out.print(GREEN_BOLD + visualLocal + RESET);
                else
                    System.out.print(visualLocal);

                // Rellenamos con espacios hasta completar el ancho
                // Si el nombre es muy largo, cortamos o dejamos que empuje (aqui dejamos que
                // empuje min 1 espacio)
                int paddingLocal = Math.max(1, colWidth - visualLocal.length());
                System.out.print(" ".repeat(paddingLocal));

                // 2. Imprimir Marcador
                System.out.printf("%d - %d", partido.getGolesLocal(), partido.getGolesVisitante());

                // 3. Imprimir Visitante (Alineado a la DERECHA)
                int paddingVisit = Math.max(1, colWidth - visualVisit.length());
                System.out.print(" ".repeat(paddingVisit));

                if (visitIsUser)
                    System.out.println(GREEN_BOLD + visualVisit + RESET);
                else
                    System.out.println(visualVisit);
            }
            System.out.println("-------------------------------------------------------");
        }
    }

    // ============================================================
    // SIMULAR JORNADA
    // ============================================================
    private void simularJornada(Usuario usuario) {
        List<Equipo> equipos = leagueRepository.listarEquipos();
        int totalEquipos = equipos.size();

        if (totalEquipos < 2 || totalEquipos % 2 != 0) {
            System.out.println("No hay suficientes equipos (o número impar) para generar el calendario.");
            return;
        }

        // 1. Calcular número de jornada siguiente
        List<Jornada> jornadasJugadas = leagueRepository.listarJornadas();
        int nextJornadaNumber = jornadasJugadas.stream()
                .mapToInt(Jornada::getNumJornada)
                .max()
                .orElse(0) + 1;

        int jornadasPorVuelta = totalEquipos - 1;
        int totalJornadasLiga = jornadasPorVuelta * 2;

        if (nextJornadaNumber > totalJornadasLiga) {
            System.out.println("¡La Liga ha finalizado! Se han jugado todas las jornadas (" + totalJornadasLiga + ").");
            return;
        }

        System.out.println("Simulando jornada " + nextJornadaNumber + " de " + totalJornadasLiga + "...");

        SimuladorJornada simulador = new SimuladorJornada();

        // 2. Generar emparejamientos DETERMINISTAS (Algoritmo Circular / Berger)
        List<Partido> partidos = generarPartidosBerger(nextJornadaNumber, equipos);

        // 3. Simular resultados
        Jornada jornadaSimulada = simulador.simularJornada(nextJornadaNumber, partidos);

        // 4. Guardar
        leagueRepository.guardarJornada(jornadaSimulada);

        // 5. Mostrar
        System.out.println("\n=== RESULTADOS JORNADA " + jornadaSimulada.getNumJornada() + " ===");

        final String GREEN_BOLD = "\u001B[1;32m";
        final String RESET = "\u001B[0m";
        String userTeamId = (usuario != null) ? usuario.getEquipo() : null;

        for (Partido p : jornadaSimulada.getPartidos()) {
            boolean localIsUser = userTeamId != null && userTeamId.equals(p.getEquipoLocal().getId());
            boolean visitIsUser = userTeamId != null && userTeamId.equals(p.getEquipoVisitante().getId());

            String nombreLocalOriginal = p.getEquipoLocal().getNombre();
            String nombreVisitOriginal = p.getEquipoVisitante().getNombre();

            String visualLocal = localIsUser ? "* " + nombreLocalOriginal : nombreLocalOriginal;
            String visualVisit = visitIsUser ? "* " + nombreVisitOriginal : nombreVisitOriginal;

            int colWidth = 25;

            // Local
            if (localIsUser)
                System.out.print(GREEN_BOLD + visualLocal + RESET);
            else
                System.out.print(visualLocal);

            int paddingLocal = Math.max(1, colWidth - visualLocal.length());
            System.out.print(" ".repeat(paddingLocal));

            // Marcador
            System.out.printf("%d - %d", p.getGolesLocal(), p.getGolesVisitante());

            // Visitante
            int paddingVisit = Math.max(1, colWidth - visualVisit.length());
            System.out.print(" ".repeat(paddingVisit));

            if (visitIsUser)
                System.out.println(GREEN_BOLD + visualVisit + RESET);
            else
                System.out.println(visualVisit);

            // Goleadores
            if (p.getGoles() != null) {
                p.getGoles().stream()
                        .sorted(Comparator.comparingInt(Gol::getMinuto))
                        .forEach(g -> {
                            boolean isUserPlayer = userTeamId != null
                                    && userTeamId.equals(g.getJugador().getEquipoId());
                            if (isUserPlayer)
                                System.out.print(GREEN_BOLD);
                            System.out.printf("   Min %d' - %s (%s)%n",
                                    g.getMinuto(),
                                    g.getJugador().getNombre(),
                                    g.getJugador().getEquipoId()
                                            .equals(p.getEquipoLocal().getId())
                                                    ? "Local"
                                                    : "Visitante");
                            if (isUserPlayer)
                                System.out.print(RESET);
                        });
            }
            System.out.println("--------------------------------");
        }

        System.out.println("✔ Jornada simulada y guardada.\n");
    }

    /**
     * Genera los partidos correspondientes a una jornada específica usando el
     * sistema de Liga (Todos contra todos).
     * Garantiza ida y vuelta y evita repeticiones aleatorias.
     */
    private List<Partido> generarPartidosBerger(int numJornada, List<Equipo> equipos) {
        int n = equipos.size();
        int rondasIda = n - 1;

        // Ordenamos la lista por ID para que el algoritmo sea siempre consistente,
        // sin importar el orden en que vengan del repositorio.
        List<Equipo> sorted = new ArrayList<>(equipos);
        sorted.sort(Comparator.comparing(Equipo::getId));

        boolean esVuelta = numJornada > rondasIda;
        int rondaIndex = (numJornada - 1) % rondasIda;

        // Algoritmo circular:
        // Separamos al primer equipo (fijo) y rotamos al resto 'rondaIndex' veces.
        Equipo fijo = sorted.remove(0);
        Collections.rotate(sorted, rondaIndex);
        sorted.add(0, fijo); // Lo volvemos a poner al principio

        List<Partido> partidos = new ArrayList<>();

        // Emparejamos extremos: 0 con N-1, 1 con N-2...
        // 0 1 2 ...
        // 5 4 3 ...
        for (int i = 0; i < n / 2; i++) {
            Equipo local = sorted.get(i);
            Equipo visit = sorted.get(n - 1 - i);

            // Alternancia de localía para el equipo fijo (índice 0) para equilibrar
            if (i == 0) {
                if (rondaIndex % 2 != 0) { // En rondas impares (0-indexed), invertimos
                    Equipo tmp = local;
                    local = visit;
                    visit = tmp;
                }
            } else {
                // Para el resto de pares, el esquema base es: el de la "fila de arriba" juega
                // en casa en la ida.
                // En nuestra lista plana 'sorted', fila de arriba son los indices i (0..N/2-1).
                // Así que 'local' ya es sorted.get(i). Correcto.
            }

            // Si estamos en la segunda vuelta, invertimos SIEMPRE la localía base de la ida
            if (esVuelta) {
                Equipo tmp = local;
                local = visit;
                visit = tmp;
            }

            partidos.add(new Partido(local, visit));
        }
        return partidos;
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
