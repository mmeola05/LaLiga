package com.liga.util;

import java.util.*;
import com.liga.model.Alineacion;
import com.liga.model.Jugador;
import com.liga.model.Posicion;

public class AlineacionGenerator {

    public static Alineacion generar(List<Jugador> jugadoresEquipo) {

        Random rnd = new Random();

        // Separar por posición
        List<Jugador> porteros = new ArrayList<>();
        List<Jugador> defensas = new ArrayList<>();
        List<Jugador> medios = new ArrayList<>();
        List<Jugador> delanteros = new ArrayList<>();

        for (Jugador j : jugadoresEquipo) {
            switch (j.getPosicion()) {
                case PORTERO -> porteros.add(j);
                case DEFENSA -> defensas.add(j);
                case MEDIO -> medios.add(j);
                case DELANTERO -> delanteros.add(j);
            }
        }

        // Elegir aleatorios
        Jugador portero = porteros.get(rnd.nextInt(porteros.size()));

        List<Jugador> def = elegir(defensas, 4, rnd);
        List<Jugador> med = elegir(medios, 4, rnd);
        List<Jugador> del = elegir(delanteros, 2, rnd);

        Alineacion al = new Alineacion();
        al.setFormacion("4-4-2");
        al.setPortero(portero.getId());
        al.setDefensas(def.stream().map(Jugador::getId).toList());
        al.setMedios(med.stream().map(Jugador::getId).toList());
        al.setDelanteros(del.stream().map(Jugador::getId).toList());

        return al;
    }

    private static List<Jugador> elegir(List<Jugador> lista, int cantidad, Random r) {
        Collections.shuffle(lista, r);
        return lista.subList(0, cantidad);
    }
}
