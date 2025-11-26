package com.liga.repository.file;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.liga.model.Jugador;
import com.liga.repository.dao.JugadorDAO;

public class JugadorDAOImplJSON implements JugadorDAO {

   private static final String FILE_PATH = "src/main/resources/json/players.json";
   private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

   private static class Root {
      List<Jugador> jugadores;
   }

   private Root loadData() {
      Path path = Paths.get(FILE_PATH);

      if (!Files.exists(path)) {
         return new Root();
      }

      try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
         Root root = gson.fromJson(reader, Root.class);
         if (root == null)
            return new Root();
         if (root.jugadores == null)
            root.jugadores = new ArrayList<>();
         return root;
      } catch (IOException e) {
         throw new RuntimeException("Error al leer el archivo JSON", e);
      }
   }

   private void saveData(Root root) {
      Path path = Paths.get(FILE_PATH);

      try {
         Files.createDirectories(path.getParent());
      } catch (IOException e) {
         throw new RuntimeException("No se pudo crear el directorio de datos", e);
      }

      try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
         gson.toJson(root, writer);
      } catch (IOException e) {
         throw new RuntimeException("Error al escribir en el archivo JSON", e);
      }
   }

   @Override
   public List<Jugador> findAll() {
      return new ArrayList<>(loadData().jugadores);
   }

   @Override
   public List<Jugador> findByEquipo(String idEquipo) {
      return loadData().jugadores.stream()
            .filter(j -> idEquipo.equals(j.getEquipoId()))
            .toList();
   }

   @Override
   public Optional<Jugador> findById(String id) {
      return loadData().jugadores.stream()
            .filter(j -> j.getId().equals(id))
            .findFirst();
   }

   @Override
   public void save(Jugador jugador) {
      Root root = loadData();
      root.jugadores.removeIf(j -> j.getId().equals(jugador.getId()));
      root.jugadores.add(jugador);
      saveData(root);
   }

   @Override
   public void saveAll(List<Jugador> jugadores) {
      Root root = loadData();
      for (Jugador jugador : jugadores) {
         root.jugadores.removeIf(j -> j.getId().equals(jugador.getId()));
         root.jugadores.add(jugador);
      }
      saveData(root);
   }

   @Override
   public void deleteById(String id) {
      Root root = loadData();
      boolean removed = root.jugadores.removeIf(e -> e.getId().equals(id));
      if (removed) {
         saveData(root);
      }
   }
}
