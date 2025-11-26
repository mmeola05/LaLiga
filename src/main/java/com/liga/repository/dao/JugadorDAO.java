package com.liga.repository.dao;

import com.liga.model.Jugador;

import java.util.List;
import java.util.Optional;

public interface JugadorDAO {
    List<Jugador> findAll();
    List<Jugador> findByEquipo(String idEquipo);
    Optional<Jugador> findById(String id);
    void save(Jugador jugador);
    void saveAll(List<Jugador> jugadores);
    void deleteById(String id);
}
