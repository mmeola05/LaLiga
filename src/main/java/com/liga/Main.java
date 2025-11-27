package com.liga;

import com.liga.controller.VistaPrincipalController;
import com.liga.view.cli.MenuPrincipal;

public class Main {
    public static void main(String[] args) {
        MenuPrincipal menu = new MenuPrincipal();
        VistaPrincipalController controller = new VistaPrincipalController(menu);
        controller.ejecutarMenu();

    }
}