package com.liga;

import com.liga.view.cli.MenuPrincipal;

public class Main {
    public static void main(String[] args) {
        try {
            // La selección de persistencia ahora se maneja dentro de MenuPrincipal
            new MenuPrincipal().iniciarApp();
        } catch (Exception e) {
            System.err.println("CRASH FATAL: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
