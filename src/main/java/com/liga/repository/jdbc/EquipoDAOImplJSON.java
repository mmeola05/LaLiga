package com.liga.repository.jdbc;

import com.google.gson.Gson;
import com.liga.model.Equipo;
import com.liga.repository.dao.EquipoDAO;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
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

   private Root loadData() throws IOException {
      try (Reader reader = new FileReader(FILE_PATH)) {
         return gson.fromJson(reader, Root.class);
      } catch (IOException e) {
         e.printStackTrace();
         return new Root();
      }
   }

   private void saveData(Root root) throws IOException {
      try (Writer writer = new FileWriter(FILE_PATH)) {
         gson.toJson(root, writer);
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   @Override
   public List<Equipo> findAll() {
      try {
         Root root = loadData();
         return root.equipos != null ? root.equipos : new ArrayList<>();
      } catch (IOException e) {
         e.printStackTrace();
         return new ArrayList<>();
      }
   }

   @Override
   public Optional<Equipo> findById(String id) {
      return findAll().stream()
            .filter(e -> e.getId().equals(id))
            .findFirst();
   }

   @Override
   public void save(Equipo equipo) {
      try {
         Root root = loadData();
         if (root.equipos == null) {
            root.equipos = new ArrayList<>();
         }
         root.equipos.removeIf(e -> e.getId().equals(equipo.getId()));
         root.equipos.add(equipo);

         saveData(root);
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   @Override
   public void saveAll(List<Equipo> equipos) {
      try {
         Root root = loadData();
         if (root.equipos == null) {
            root.equipos = new ArrayList<>();
         }

         for (Equipo equipo : equipos) {
            root.equipos.removeIf(e -> e.getId().equals(equipo.getId()));
            root.equipos.add(equipo);
         }
         saveData(root);
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   @Override
   public void deleteById(String id) {
      try {
         Root root = loadData();
         if (root.equipos != null) {
            boolean removed = root.equipos.removeIf(e -> e.getId().equals(id));
            if (removed) {
               saveData(root);
            }
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
   }
}
