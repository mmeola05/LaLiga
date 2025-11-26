package com.liga.repository.dao;

import com.liga.model.Equipo;

import java.util.List;
import java.util.Optional;

public interface EquipoDAO {
    List<Equipo> findAll();
    Optional<Equipo> findById(String id);
    void save(Equipo equipo);
    void saveAll(List<Equipo> equipos);
    void deleteById(String id);
}
