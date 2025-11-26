package com.liga.repository.file;

import com.google.gson.Gson;
import com.liga.model.Equipo;
import com.liga.repository.dao.EquipoDAO;

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

public class EquipoDAOImplJSON implements EquipoDAO {

   private static final String FILE_PATH = "src/main/resources/json/teams.json";
   private final Gson gson = new Gson();

   private static class Root {
      String temporada;
      String liga;
      List<Equipo> equipos;
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
         if (root.equipos == null)
            root.equipos = new ArrayList<>();
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
   public List<Equipo> findAll() {
      return new ArrayList<>(loadData().equipos);

   }

   @Override
   public Optional<Equipo> findById(String id) {
      return loadData().equipos.stream()
            .filter(e -> id.equals(e.getId()))
            .findFirst();
   }

   @Override
   public void save(Equipo equipo) {
      Root root = loadData();
      root.equipos.removeIf(e -> e.getId().equals(equipo.getId()));
      root.equipos.add(equipo);
      saveData(root);
   }

   @Override
   public void saveAll(List<Equipo> equipos) {
      Root root = loadData();
      for (Equipo equipo : equipos) {
         root.equipos.removeIf(e -> e.getId().equals(equipo.getId()));
         root.equipos.add(equipo);
      }
      saveData(root);
   }

   @Override
   public void deleteById(String id) {
      Root root = loadData();
      boolean removed = root.equipos.removeIf(e -> e.getId().equals(id));
      if (removed) {
         saveData(root);
      }
   }
}
