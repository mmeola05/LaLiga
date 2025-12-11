package com.liga.repository.file;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.liga.model.Jornada;
import com.liga.repository.dao.JorandaDAO;

public class JornadaDAOImplJSON implements JorandaDAO {
  private static final String FILE_PATH = "src/main/resources/json/competicion.json";
  private Gson gson;

  public JornadaDAOImplJSON() {
    this.gson = new GsonBuilder().setPrettyPrinting().create();
  }

  @Override
  public List<Jornada> findAll() {
    List<Jornada> jornadas = new ArrayList<>();
    try (FileReader reader = new FileReader(FILE_PATH)) {
      Type type = new TypeToken<List<Jornada>>() {
      }.getType();
      jornadas = gson.fromJson(reader, type);
      if (jornadas == null) {
        jornadas = new ArrayList<>();
      }
    } catch (IOException e) {
      System.err.println("Error al leer competicones.json " + e.getMessage());
    }
    return jornadas;
  }

  @Override
  public Optional<Jornada> findById(int id) {
    return findAll().stream()
        .filter(jornada -> jornada.getNumJornada() == id)
        .findFirst();
  }

  @Override
  public void save(Jornada jornada) {
    List<Jornada> jornadas = findAll();
    jornadas.removeIf(j -> j.getNumJornada() == jornada.getNumJornada());
    jornadas.add(jornada);
    saveAll(jornadas);
  }

  @Override
  public void saveAll(List<Jornada> jornadas) {
    try (FileWriter writer = new FileWriter(FILE_PATH)) {
      gson.toJson(jornadas, writer);
    } catch (IOException e) {
      System.err.println("Error al escribir en competicones.json " + e.getMessage());
    }
  }
}
