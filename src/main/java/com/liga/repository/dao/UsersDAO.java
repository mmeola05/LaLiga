package com.liga.repository.dao;

import java.util.List;
import java.util.Optional;

import com.liga.model.Usuario;

public interface UsersDAO {

  List<Usuario> findAll();

  Optional<Usuario> findById(String id);

  void save(Usuario usuario);

  void saveAll(List<Usuario> usuarios);

  void deleteById(String id);
}
