package com.liga.repository;

import com.liga.model.Equipo;
import com.liga.model.Jornada;
import com.liga.model.Jugador;
import com.liga.model.Usuario;

import java.util.List;
import java.util.Optional;

public interface LeagueRepository {
        List<Equipo> listarEquipos();

        Optional<Equipo> buscarEquipoPorId(String id);

        List<Jugador> listarJugadores();

        List<Jugador> buscarJugadorPorEquipo(String equipoId);

        Optional<Jugador> buscarJugadorPorId(String id);

        List<Usuario> listarUsuarios();

        Optional<Usuario> buscarUsuarioPorId(String id);

        void guardarUsuarios(List<Usuario> usuarios);

        List<Jornada> listarJornadas();

        Optional<Jornada> buscarJornadaPorId(int id);

        void guardarJornada(Jornada jornada);
}
