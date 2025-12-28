package com.liga.service;

import com.liga.model.JugadorMercado;
import com.liga.repository.LeagueRepository;
import com.liga.model.Jugador;
import com.liga.model.Usuario;
import java.util.Optional;

import java.util.List;

public class MarketService {

    private final LeagueRepository repo;

    public MarketService(LeagueRepository repo) {
        this.repo = repo;
    }

    // ==================================================
    // E9 - Listar jugadores en el mercado
    // ==================================================
    public List<JugadorMercado> listarMercado() {
        return repo.listarMercado();
    }

    // ==================================================
    // E10 - Poner jugador en venta (PENDIENTE)
    // ==================================================
    // ==================================================
// E10 - Poner jugador en venta
// ==================================================
    public boolean ponerEnVenta(String usuarioId, String jugadorId, double precio) {

        // 1. Validar precio
        if (precio <= 0) {
            return false;
        }

        // 2. Obtener usuario
        Optional<Usuario> optUsuario = repo.buscarUsuarioPorId(usuarioId);
        if (optUsuario.isEmpty()) {
            return false;
        }
        Usuario usuario = optUsuario.get();

        // 3. Verificar que el jugador pertenece al usuario
        if (usuario.getPlantilla() == null || !usuario.getPlantilla().contains(jugadorId)) {
            return false;
        }

        // 4. Verificar que el jugador NO esté ya en el mercado
        boolean yaEnMercado = repo.listarMercado().stream()
                .anyMatch(jm -> jm.getJugadorId().equals(jugadorId));

        if (yaEnMercado) {
            return false;
        }

        // 5. Verificar que el jugador existe
        Optional<Jugador> optJugador = repo.buscarJugadorPorId(jugadorId);
        if (optJugador.isEmpty()) {
            return false;
        }

        // 6. Crear JugadorMercado
        String mercadoId = "M" + System.currentTimeMillis();

        JugadorMercado jugadorMercado = new JugadorMercado(
                jugadorId,
                precio,
                usuarioId,
                mercadoId
        );

        // Quitar jugador de la plantilla
        usuario.getPlantilla().remove(jugadorId);

        // Guardar usuario
        repo.guardarUsuarios(List.of(usuario));

        // Guardar en mercado
        repo.guardarJugadorMercado(jugadorMercado);

        return true;
    }

    // ==================================================
// E11 - Comprar jugador del mercado
// ==================================================
    public boolean comprarJugador(String compradorId, String jugadorMercadoId) {

        // 1. Obtener comprador
        Optional<Usuario> optComprador = repo.buscarUsuarioPorId(compradorId);
        if (optComprador.isEmpty()) {
            return false;
        }
        Usuario comprador = optComprador.get();

        // 2. Obtener jugador en mercado
        Optional<JugadorMercado> optJM
                = repo.buscarJugadorMercadoPorId(jugadorMercadoId);

        if (optJM.isEmpty()) {
            return false;
        }
        JugadorMercado jm = optJM.get();

        // 3. No comprar a uno mismo
        if (jm.getVendedor().equals(compradorId)) {
            return false;
        }

        // 4. Obtener vendedor
        Optional<Usuario> optVendedor
                = repo.buscarUsuarioPorId(jm.getVendedor());

        if (optVendedor.isEmpty()) {
            return false;
        }
        Usuario vendedor = optVendedor.get();

        // 5. Validar saldo
        if (comprador.getSaldo() < jm.getPrecioSalida()) {
            return false;
        }

        // 6. Validar máximo 25 jugadores
        if (comprador.getPlantilla() != null
                && comprador.getPlantilla().size() >= 25) {
            return false;
        }

        // 7. Transferir jugador
        String jugadorId = jm.getJugadorId();

        vendedor.getPlantilla().remove(jugadorId);
        comprador.getPlantilla().add(jugadorId);

        // 8. Actualizar saldos
        comprador.setSaldo(comprador.getSaldo() - jm.getPrecioSalida());
        vendedor.setSaldo(vendedor.getSaldo() + jm.getPrecioSalida());

        // 9. Guardar usuarios
        repo.guardarUsuarios(
                List.of(comprador, vendedor)
        );

        // 10. Eliminar del mercado
        repo.eliminarJugadorMercado(jugadorMercadoId);

        return true;
    }

}
