package com.liga.repository;

import com.liga.model.Equipo;
import com.liga.model.Jugador;
import com.liga.model.Usuario;

import java.util.List;
import java.util.Optional;

public class LeagueRepositoryImpl implements LeagueRepository {

    @Override
    public List<Equipo> listarEquipos() {
        return List.of();
    }

    @Override
    public Optional<Equipo> buscarEquipoPorId(String id) {
        return Optional.empty();
    }

    @Override
    public List<Jugador> listarJugadores() {
        return List.of();
    }

    @Override
    public List<Jugador> buscarJugadorPorEquipo(String equipoId) {
        return List.of();
    }

    @Override
    public Optional<Jugador> buscarJugadorPorId(String id) {
        return Optional.empty();
    }

    @Override
    public List<Usuario> listarUsuarios() {
        return List.of();
    }

    @Override
    public Optional<Usuario> buscarUsuarioPorId(String id) {
        return Optional.empty();
    }

    @Override
    public void guardarUsuarios(List<Usuario> usuarios) {

    }
}
