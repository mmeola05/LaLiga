package com.liga.service;

import com.liga.model.Alineacion;
import com.liga.model.TipoUsuario;
import com.liga.model.Usuario;
import com.liga.repository.dao.UsersDAO;
import com.liga.repository.file.UsersDAOImplJSON;
import com.liga.util.HashUtil;
import com.liga.util.UserIDGenerator;

import java.util.List;

public class UserService {

    private final UsersDAO dao = new UsersDAOImplJSON();

    public Usuario login(String email, String password) {

        String hashed = HashUtil.sha256(password);

        return dao.findAll().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .filter(u -> u.getPassword().equals(hashed))
                .findFirst()
                .orElse(null);
    }

    public Usuario registrar(String email, String password, String equipoId, Alineacion alineacion,
            List<String> plantilla) {

        boolean existe = dao.findAll().stream()
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

        dao.save(nuevo);
        return nuevo;
    }

}
