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

    // E9 Listar mercado

    public List<JugadorMercado> listarMercado() {
        return repo.listarMercado();
    }

    // E10 Poner en venta

    public boolean ponerEnVenta(String usuarioId, String jugadorId, double precio) {

        // 1. Valida precio

        if (precio <= 0) {
            return false;
        }

        // 2. Obtiene usuario

        Optional<Usuario> optUsuario = repo.buscarUsuarioPorId(usuarioId);
        if (optUsuario.isEmpty()) {
            return false;
        }
        Usuario usuario = optUsuario.get();

        // 3. Verifica propiedad

        if (usuario.getPlantilla() == null || !usuario.getPlantilla().contains(jugadorId)) {
            return false;
        }

        // 4. Verifica mercado

        boolean yaEnMercado = repo.listarMercado().stream()
                .anyMatch(jm -> jm.getJugadorId().equals(jugadorId));

        if (yaEnMercado) {
            return false;
        }

        // 5. Verifica existencia

        Optional<Jugador> optJugador = repo.buscarJugadorPorId(jugadorId);
        if (optJugador.isEmpty()) {
            return false;
        }

        // 6. Crea JugadorMercado

        String mercadoId = "M" + System.currentTimeMillis();

        JugadorMercado jugadorMercado = new JugadorMercado(
                jugadorId,
                precio,
                usuarioId,
                mercadoId);

        // Quitar jugador de la plantilla
        usuario.getPlantilla().remove(jugadorId);

        // Guardar usuario
        repo.guardarUsuarios(List.of(usuario));

        // Guardar en mercado
        repo.guardarJugadorMercado(jugadorMercado);

        return true;
    }

    // E11 Comprar jugador

    public boolean comprarJugador(String compradorId, String jugadorMercadoId) {

        // 1. Obtiene comprador

        Optional<Usuario> optComprador = repo.buscarUsuarioPorId(compradorId);
        if (optComprador.isEmpty()) {
            return false;
        }
        Usuario comprador = optComprador.get();

        // 2. Obtiene jugador

        Optional<JugadorMercado> optJM = repo.buscarJugadorMercadoPorId(jugadorMercadoId);

        if (optJM.isEmpty()) {
            return false;
        }
        JugadorMercado jm = optJM.get();

        // 3. Verifica mismo usuario

        if (jm.getVendedor().equals(compradorId)) {
            return false;
        }

        // 4. Obtiene vendedor

        Optional<Usuario> optVendedor = repo.buscarUsuarioPorId(jm.getVendedor());

        if (optVendedor.isEmpty()) {
            return false;
        }
        Usuario vendedor = optVendedor.get();

        // 5. Valida saldo

        if (comprador.getSaldo() < jm.getPrecioSalida()) {
            return false;
        }

        // 6. Valida limite plantilla

        if (comprador.getPlantilla() != null
                && comprador.getPlantilla().size() >= 25) {
            return false;
        }

        // 7. Transfiere jugador

        String jugadorId = jm.getJugadorId();

        if (vendedor.getPlantilla() == null)
            vendedor.setPlantilla(new java.util.ArrayList<>());
        vendedor.getPlantilla().remove(jugadorId);

        if (comprador.getPlantilla() == null)
            comprador.setPlantilla(new java.util.ArrayList<>());
        comprador.getPlantilla().add(jugadorId);

        // 8. Actualiza saldos

        comprador.setSaldo(comprador.getSaldo() - jm.getPrecioSalida());
        vendedor.setSaldo(vendedor.getSaldo() + jm.getPrecioSalida());

        // 9. Guarda usuarios

        repo.guardarUsuarios(
                List.of(comprador, vendedor));

        // 10. Elimina del mercado

        repo.eliminarJugadorMercado(jugadorMercadoId);

        return true;
    }

}
