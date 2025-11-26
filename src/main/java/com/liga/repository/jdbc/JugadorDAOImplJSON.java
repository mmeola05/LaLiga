package com.liga.repository.jdbc;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.gson.Gson;
import com.liga.model.Jugador;
import com.liga.repository.dao.JugadorDAO;

public class JugadorDAOImplJSON implements JugadorDAO {

   private static final String FILE_PATH = "src/main/resources/json/players.json";
   private final Gson gson = new Gson();

   private static class Root {
      List<Jugador> jugadores;
   }

   private Root loadData() {
      try (Reader reader = new FileReader(FILE_PATH)) {
         return gson.fromJson(reader, Root.class);
      } catch (IOException e) {
         e.printStackTrace();
         return new Root();
      }
   }

   private void saveData(Root root) {
      try (Writer writer = new FileWriter(FILE_PATH)) {
         gson.toJson(root, writer);
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   @Override
   public List<Jugador> findAll() {
      Root root = loadData();
      return root.jugadores != null ? root.jugadores : new ArrayList<>();
   }

   @Override
   public List<Jugador> findByEquipo(String idEquipo) {
      return findAll().stream()
            .filter(j -> idEquipo.equals(j.getEquipoId()))
            .toList();
   }

   @Override
   public Optional<Jugador> findById(String id) {
      return findAll().stream()
            .filter(j -> j.getId().equals(id))
            .findFirst();
   }

   @Override
   public void save(Jugador jugador) {
      Root root = loadData();
      if (root.jugadores == null) {
         root.jugadores = new ArrayList<>();
      }
      root.jugadores.removeIf(j -> j.getId().equals(jugador.getId()));
      root.jugadores.add(jugador);
      saveData(root);
   }

   @Override
   public void saveAll(List<Jugador> jugadores) {
      Root root = loadData();
      if (root.jugadores == null) {
         root.jugadores = new ArrayList<>();
      }
      for (Jugador jugador : jugadores) {
         root.jugadores.removeIf(j -> j.getId().equals(jugador.getId()));
         root.jugadores.add(jugador);
      }
      saveData(root);
   }

   @Override
   public void deleteById(String id) {
      Root root = loadData();
      if (root.jugadores != null) {
         boolean removed = root.jugadores.removeIf(j -> j.getId().equals(id));
         if (removed) {
            saveData(root);
         }
      }
   }

}
