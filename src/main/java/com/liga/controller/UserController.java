package com.liga.controller;

import com.liga.model.Alineacion;
import com.liga.model.Usuario;
import com.liga.service.UserService;

import java.util.List;

public class UserController {

    private final UserService userService = new UserService();

    public Usuario login(String email, String password) {
        return userService.login(email, password);
    }

    public Usuario registrar(String email, String password, String equipoId, Alineacion alineacion, List<String> plantilla) {
        return userService.registrar(email, password, equipoId, alineacion, plantilla);
    }

}
