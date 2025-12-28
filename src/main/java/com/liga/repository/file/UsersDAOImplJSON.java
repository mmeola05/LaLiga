package com.liga.repository.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.liga.model.Usuario;
import com.liga.repository.dao.UsersDAO;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsersDAOImplJSON implements UsersDAO {

    private static final String FILE_PATH = "data/users.json";

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static class Root {

        String temporada;
        String liga;
        List<Usuario> usuarios = new ArrayList<>();
    }

    private Root loadData() {
        Path path = Paths.get(FILE_PATH);

        if (!Files.exists(path)) {
            return new Root();
        }

        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            Root root = gson.fromJson(reader, Root.class);
            if (root == null) {
                return new Root();
            }
            if (root.usuarios == null) {
                root.usuarios = new ArrayList<>();
            }
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
    public List<Usuario> findAll() {
        return new ArrayList<>(loadData().usuarios);
    }

    @Override
    public Optional<Usuario> findById(String id) {
        return loadData().usuarios.stream()
                .filter(e -> e.getId().equals(id))
                .findFirst();
    }

    @Override
    public void save(Usuario usuario) {
        Root root = loadData();
        root.usuarios.removeIf(e -> e.getId().equals(usuario.getId()));
        root.usuarios.add(usuario);
        saveData(root);
    }

    @Override
    public void saveAll(List<Usuario> usuariosNuevos) {
        Root root = loadData();
        for (Usuario u : usuariosNuevos) {
            root.usuarios.removeIf(e -> e.getId().equals(u.getId()));
            root.usuarios.add(u);
        }
        saveData(root);
    }

    @Override
    public void deleteById(String id) {
        Root root = loadData();
        boolean removed = root.usuarios.removeIf(e -> e.getId().equals(id));
        if (removed) {
            saveData(root);
        }
    }
}
