package com.liga.repository;

import com.liga.model.Equipo;
import com.liga.model.Jugador;
import com.liga.model.Usuario;
import com.liga.repository.dao.EquipoDAO;
import com.liga.repository.dao.JugadorDAO;
import com.liga.repository.dao.MarketDAO;
import com.liga.repository.dao.UsersDAO;

import java.util.List;
import java.util.Optional;

public class LeagueRepositoryImpl implements LeagueRepository {
    private final EquipoDAO equipoDAO;
    private final JugadorDAO jugadorDAO;
    private final MarketDAO marketDAO;
    private final UsersDAO usersDAO;

    public LeagueRepositoryImpl(EquipoDAO equipoDAO, JugadorDAO jugadorDAO, MarketDAO marketDAO, UsersDAO usersDAO) {
        this.equipoDAO = equipoDAO;
        this.jugadorDAO = jugadorDAO;
        this.marketDAO = marketDAO;
        this.usersDAO = usersDAO;
    }

    @Override
    public List<Equipo> listarEquipos() {
        return equipoDAO.findAll();
    }

    @Override
    public Optional<Equipo> buscarEquipoPorId(String id) {
        return equipoDAO.findById(id);
    }

    @Override
    public List<Jugador> listarJugadores() {
        return jugadorDAO.findAll();
    }

    @Override
    public List<Jugador> buscarJugadorPorEquipo(String equipoId) {
        return jugadorDAO.findByEquipo(equipoId);
    }

    @Override
    public Optional<Jugador> buscarJugadorPorId(String id) {
        return jugadorDAO.findById(id);
    }

    @Override
    public List<Usuario> listarUsuarios() {
        return usersDAO.findAll();
    }

    @Override
    public Optional<Usuario> buscarUsuarioPorId(String id) {
        return Optional.empty();
    }

    @Override
    public void guardarUsuarios(List<Usuario> usuarios) {
        usersDAO.saveAll(usuarios);
    }
}
