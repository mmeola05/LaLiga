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
import com.liga.model.JugadorMercado;
import com.liga.repository.dao.MarketDAO;

public class MarketDAOImplJSON implements MarketDAO {
  private static final String FILE_PATH = "src/main/resources/json/market.json";

  private final Gson gson = new Gson();

  private static class Root {
    String temporada;
    String liga;
    List<JugadorMercado> jugadoresMercado;
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
      if (root.jugadoresMercado == null)
        root.jugadoresMercado = new ArrayList<>();
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
  public Optional<JugadorMercado> findJugadorMercadoById(String idJugadorMercado) {
    return loadData().jugadoresMercado.stream()
        .filter(j -> idJugadorMercado.equals(j.getId()))
        .findFirst();
  }

  @Override
  public List<JugadorMercado> findAllJugadoresMercados() {
    return new ArrayList<>(loadData().jugadoresMercado);
  }

  @Override
  public void saveJugadorMercado(JugadorMercado jugadorMercado) {
    Root root = loadData();
    root.jugadoresMercado.removeIf(j -> j.getId().equals(jugadorMercado.getId()));
    root.jugadoresMercado.add(jugadorMercado);
    saveData(root);
  }

  @Override
  public void deleteJugadorMercadoById(String idJugadorMercado) {
    Root root = loadData();
    boolean removed = root.jugadoresMercado.removeIf(j -> j.getId().equals(idJugadorMercado));
    if (removed) {
      saveData(root);
    }
  }

}
