package com.liga.view.cli;

import java.util.Scanner;

public class MenuPrincipal {

    Scanner sc = new Scanner(System.in);

    public void mostrarMenu() {
        System.out.println("=== LIGA FANTASY ===");
        System.out.println("1. Usuarios");
        System.out.println("2. Equipos");
        System.out.println("3. Jugadores");
        System.out.println("4. Mercado");
        System.out.println("5. Alineaciones");
        System.out.println("6. Jornadas");
        System.out.println("7. Simular Jornadas");
        System.out.println("0. Salir");
        System.out.print("Elige una opción: ");
    }

    public int menuUsuarios() {
        System.out.println("\n=== USUARIOS ===");
        System.out.println("1. Iniciar sesión");
        System.out.println("2. Registrarse");
        System.out.println("0. Volver");
        System.out.print("Opción: ");
        return sc.nextInt();
    }
}
