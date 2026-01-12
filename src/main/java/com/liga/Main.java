package com.liga;

import com.liga.repository.Backend;
import com.liga.view.cli.MenuPrincipal;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        
        System.out.println("=================================");
        System.out.println("   LA LIGA MANAGER - STARTUP");
        System.out.println("=================================");
        System.out.println("Selecciona modo de persistencia:");
        System.out.println("1. JSON (Archivos locales)");
        System.out.println("2. PostgreSQL (Base de Datos)");
        System.out.print("Opción: ");

        Backend backend = Backend.JSON; // Default
        try {
            String input = sc.nextLine();
            if (input.trim().equals("2")) {
                backend = Backend.DB;
            }
        } catch (Exception e) {
            System.out.println("Error leyendo opción. Usando JSON por defecto.");
        }

        System.out.println("Iniciando aplicación con backend: " + backend);
        
        try {
            new MenuPrincipal(backend).iniciarApp();
        } catch (Exception e) {
            System.err.println("CRASH FATAL: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
