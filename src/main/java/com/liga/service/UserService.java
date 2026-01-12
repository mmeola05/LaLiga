package com.liga.service;

import com.liga.model.Alineacion;
import com.liga.model.TipoUsuario;
import com.liga.model.Usuario;
import com.liga.repository.LeagueRepository;
import com.liga.util.HashUtil;
import com.liga.util.UserIDGenerator;

import java.util.List;

public class UserService {

    private final LeagueRepository repo;

    public UserService(LeagueRepository repo) {
        this.repo = repo;
    }

    public Usuario login(String email, String password) {

        String hashed = HashUtil.sha256(password);

        return repo.listarUsuarios().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .filter(u -> u.getPassword().equals(hashed))
                .findFirst()
                .orElse(null);
    }

    public Usuario registrar(String email, String password, String equipoId, Alineacion alineacion,
            List<String> plantilla) {

        boolean existe = repo.listarUsuarios().stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(email));

        if (existe)
            return null;

        Usuario nuevo = new Usuario(
                UserIDGenerator.nextId(),
                TipoUsuario.ESTANDAR,
                email,
                HashUtil.sha256(password),
                50.0,
                equipoId,
                alineacion);

        nuevo.setPlantilla(plantilla);

        repo.guardarUsuarios(List.of(nuevo));
        return nuevo;
    }

}
