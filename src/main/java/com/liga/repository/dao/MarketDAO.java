package com.liga.repository.dao;

import java.util.List;
import java.util.Optional;

import com.liga.model.JugadorMercado;

public interface MarketDAO {
  Optional<JugadorMercado> findJugadorMercadoById(String idJugadorMercado);

  List<JugadorMercado> findAllJugadoresMercados();

  void saveJugadorMercado(JugadorMercado jugadorMercado);

  void deleteJugadorMercadoById(String idJugadorMercado);
}
