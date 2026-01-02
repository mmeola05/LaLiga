package com.liga.repository.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.liga.model.Jornada;
import com.liga.repository.dao.JorandaDAO;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JornadaDAOImplJSON implements JorandaDAO {

    private static final String FILE_PATH = "src/main/resources/json/competicion.json";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public List<Jornada> findAll() {
        Path path = Paths.get(FILE_PATH);

        if (!Files.exists(path)) {
            return new ArrayList<>();
        }

        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            Type type = new TypeToken<List<Jornada>>() {
            }.getType();
            List<Jornada> jornadas = gson.fromJson(reader, type);
            return jornadas != null ? jornadas : new ArrayList<>();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public Optional<Jornada> findById(int id) {
        return findAll().stream()
                .filter(j -> j.getNumJornada() == id)
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
        Path path = Paths.get(FILE_PATH);

        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                gson.toJson(jornadas, writer);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al escribir competicion.json", e);
        }
    }
}
