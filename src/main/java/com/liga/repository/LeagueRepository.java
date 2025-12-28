package com.liga.repository;

import com.liga.model.Equipo;
import com.liga.model.Jornada;
import com.liga.model.Jugador;
import com.liga.model.Usuario;
import com.liga.model.JugadorMercado;
import java.util.List;
import java.util.Optional;

public interface LeagueRepository {

    // Equipos / jugadores
    List<Equipo> listarEquipos();
    Optional<Equipo> buscarEquipoPorId(String id);
    List<Jugador> listarJugadores();
    List<Jugador> buscarJugadorPorEquipo(String equipoId);
    Optional<Jugador> buscarJugadorPorId(String id);

    // Usuarios
    List<Usuario> listarUsuarios();
    Optional<Usuario> buscarUsuarioPorId(String id);
    void guardarUsuarios(List<Usuario> usuarios);

    // Jornadas
    List<Jornada> listarJornadas();
    Optional<Jornada> buscarJornadaPorId(int id);
    void guardarJornada(Jornada jornada);

    // MERCADO
    List<JugadorMercado> listarMercado();
    Optional<JugadorMercado> buscarJugadorMercadoPorId(String id);
    void guardarJugadorMercado(JugadorMercado jugadorMercado);
    void eliminarJugadorMercado(String id);
}

