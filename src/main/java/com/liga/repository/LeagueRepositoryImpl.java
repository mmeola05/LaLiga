package com.liga.repository;

import com.liga.model.Equipo;
import com.liga.model.Jornada;
import com.liga.model.Jugador;
import com.liga.model.Usuario;
import com.liga.model.JugadorMercado;
import com.liga.repository.dao.EquipoDAO;
import com.liga.repository.dao.JorandaDAO;
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
    private final JorandaDAO jornadaDAO;

    public LeagueRepositoryImpl(EquipoDAO equipoDAO, JugadorDAO jugadorDAO, MarketDAO marketDAO, UsersDAO usersDAO,
            JorandaDAO jorandaDAO) {
        this.equipoDAO = equipoDAO;
        this.jugadorDAO = jugadorDAO;
        this.marketDAO = marketDAO;
        this.usersDAO = usersDAO;
        this.jornadaDAO = jorandaDAO;
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
        return usersDAO.findById(id);
    }

    @Override
    public void guardarUsuarios(List<Usuario> usuarios) {
        usersDAO.saveAll(usuarios);
    }

    @Override
    public List<Jornada> listarJornadas() {
        return jornadaDAO.findAll();
    }

    @Override
    public Optional<Jornada> buscarJornadaPorId(int id) {
        return jornadaDAO.findById(id);
    }

    @Override
    public void guardarJornada(Jornada jornada) {
        jornadaDAO.save(jornada);
    }

    @Override
    public List<JugadorMercado> listarMercado() {
        return marketDAO.findAllJugadoresMercados();
    }

    @Override
    public Optional<JugadorMercado> buscarJugadorMercadoPorId(String id) {
        return marketDAO.findJugadorMercadoById(id);
    }

    @Override
    public void guardarJugadorMercado(JugadorMercado jugadorMercado) {
        marketDAO.saveJugadorMercado(jugadorMercado);
    }

    @Override
    public void eliminarJugadorMercado(String id) {
        marketDAO.deleteJugadorMercadoById(id);
    }

}
