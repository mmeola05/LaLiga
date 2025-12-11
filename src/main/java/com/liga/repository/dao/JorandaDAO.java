package com.liga.repository.dao;

import java.util.List;
import java.util.Optional;

import com.liga.model.Jornada;

public interface JorandaDAO {
  List<Jornada> findAll();

  Optional<Jornada> findById(int id);

  void save(Jornada jornada);

  void saveAll(List<Jornada> jornadas);
}
