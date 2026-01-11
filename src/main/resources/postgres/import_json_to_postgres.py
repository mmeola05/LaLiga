#!/usr/bin/env python3
"""
import_json_to_postgres.py
Importa users.json, teams.json, players.json, market.json y competicion.json
a la base de datos PostgreSQL definida en las variables de conexión.
"""

import json
import os
import psycopg2
from psycopg2.extras import execute_values

# Configuración de conexión (ajusta si tu docker-compose usa otros credenciales)
DB_HOST = os.environ.get("DB_HOST", "localhost")
DB_PORT = os.environ.get("DB_PORT", "5432")
DB_NAME = os.environ.get("DB_NAME", "la_liga")
DB_USER = os.environ.get("DB_USER", "admin")
DB_PASS = os.environ.get("DB_PASS", "admin")

# Rutas de los ficheros (deben estar en la misma carpeta del script o ajustar)
FILES = {
    "users": "users.json",
    "teams": "teams.json",
    "players": "players.json",
    "market": "market.json",
    "competicion": "competicion.json"
}

def load_json(path):
    with open(path, "r", encoding="utf-8") as f:
        return json.load(f)

DDL = [
# (coloco DDL en el orden necesario para FKs)
"""
CREATE TABLE IF NOT EXISTS equipos (
  id VARCHAR(10) PRIMARY KEY,
  nombre TEXT NOT NULL
);
""",
"""
CREATE TABLE IF NOT EXISTS liga (
  id VARCHAR(50) PRIMARY KEY,
  temporada VARCHAR(20),
  nombre_liga TEXT
);
""",
"""
CREATE TABLE IF NOT EXISTS jugadores (
  id VARCHAR(10) PRIMARY KEY,
  nombre TEXT NOT NULL,
  posicion VARCHAR(30),
  equipo_id VARCHAR(10) REFERENCES equipos(id),
  precio NUMERIC(10,2),
  ataque INT,
  defensa INT,
  pase INT,
  porteria INT,
  condicion INT
);
""",
"""
CREATE TABLE IF NOT EXISTS usuarios (
  id VARCHAR(10) PRIMARY KEY,
  tipo VARCHAR(30),
  email TEXT UNIQUE NOT NULL,
  password TEXT NOT NULL,
  saldo NUMERIC(12,2),
  equipo_id VARCHAR(10) REFERENCES equipos(id)
);
""",
"""
CREATE TABLE IF NOT EXISTS alineaciones (
  usuario_id VARCHAR(10) PRIMARY KEY REFERENCES usuarios(id),
  formacion VARCHAR(20),
  portero VARCHAR(10)
);
""",
"""
CREATE TABLE IF NOT EXISTS alineacion_posiciones (
  usuario_id VARCHAR(10) REFERENCES usuarios(id),
  posicion_tipo VARCHAR(20),
  orden INT,
  jugador_id VARCHAR(10) REFERENCES jugadores(id),
  PRIMARY KEY (usuario_id, posicion_tipo, orden)
);
""",
"""
CREATE TABLE IF NOT EXISTS plantillas (
  usuario_id VARCHAR(10) REFERENCES usuarios(id),
  jugador_id VARCHAR(10) REFERENCES jugadores(id),
  PRIMARY KEY (usuario_id, jugador_id)
);
""",
"""
CREATE TABLE IF NOT EXISTS mercado (
  id VARCHAR(20) PRIMARY KEY,
  jugador_id VARCHAR(10) REFERENCES jugadores(id),
  precio_salida NUMERIC(10,2),
  vendedor VARCHAR(10) REFERENCES usuarios(id)
);
""",
"""
CREATE TABLE IF NOT EXISTS jornadas (
  id SERIAL PRIMARY KEY,
  num_jornada INT NOT NULL UNIQUE
);
""",
"""
CREATE TABLE IF NOT EXISTS partidos (
  id SERIAL PRIMARY KEY,
  jornada_id INT REFERENCES jornadas(id),
  equipo_local VARCHAR(10) REFERENCES equipos(id),
  equipo_visitante VARCHAR(10) REFERENCES equipos(id),
  goles_local INT,
  goles_visitante INT
);
""",
"""
CREATE TABLE IF NOT EXISTS partido_equipo (
  partido_id INT REFERENCES partidos(id),
  equipo_id VARCHAR(10) REFERENCES equipos(id),
  es_local BOOLEAN,
  partidos_jugados INT,
  victorias INT,
  empates INT,
  derrotas INT,
  goles_favor INT,
  goles_contra INT,
  puntos INT,
  PRIMARY KEY (partido_id, equipo_id)
);
""",
"""
CREATE TABLE IF NOT EXISTS goles (
  id SERIAL PRIMARY KEY,
  partido_id INT REFERENCES partidos(id),
  jugador_id VARCHAR(10) REFERENCES jugadores(id),
  minuto INT,
  equipo_id VARCHAR(10) REFERENCES equipos(id)
);
"""
]

def main():
    # Validar ficheros
    for key, path in FILES.items():
        if not os.path.isfile(path):
            print(f"ERROR: falta el fichero {path}. Colócalo en la carpeta y vuelve a ejecutar.")
            return

    conn = psycopg2.connect(host=DB_HOST, port=DB_PORT, dbname=DB_NAME, user=DB_USER, password=DB_PASS)
    conn.autocommit = True
    cur = conn.cursor()

    # 1) Crear ddl
    for s in DDL:
        cur.execute(s)

    # 2) Cargar teams.json -> equipos + liga
    teams = load_json(FILES["teams"])
    # teams esperado: { "temporada": "...", "liga": "...", "equipos": [ {...} ] }
    temporada = teams.get("temporada")
    nombre_liga = teams.get("liga")
    liga_id = f"{nombre_liga}_{temporada}".replace(" ", "_")
    cur.execute("INSERT INTO liga (id, temporada, nombre_liga) VALUES (%s,%s,%s) ON CONFLICT (id) DO NOTHING",
                (liga_id, temporada, nombre_liga))
    equipos_list = teams.get("equipos", [])
    equipo_vals = [(e["id"], e["nombre"]) for e in equipos_list]
    if equipo_vals:
        execute_values(cur,
                       "INSERT INTO equipos (id,nombre) VALUES %s ON CONFLICT (id) DO UPDATE SET nombre = EXCLUDED.nombre",
                       equipo_vals)

    # 3) Cargar players.json -> jugadores
    players = load_json(FILES["players"])
    jugadores_arr = players.get("jugadores", [])
    jugador_vals = []
    for p in jugadores_arr:
        jugador_vals.append((
            p.get("id"),
            p.get("nombre"),
            p.get("posicion"),
            p.get("equipoId"),
            p.get("precio"),
            p.get("ataque"),
            p.get("defensa"),
            p.get("pase"),
            p.get("porteria"),
            p.get("condition")
        ))
    if jugador_vals:
        execute_values(cur,
            """
            INSERT INTO jugadores (id,nombre,posicion,equipo_id,precio,ataque,defensa,pase,porteria,condicion)
            VALUES %s
            ON CONFLICT (id) DO UPDATE SET
              nombre = EXCLUDED.nombre,
              posicion = EXCLUDED.posicion,
              equipo_id = EXCLUDED.equipo_id,
              precio = EXCLUDED.precio,
              ataque = EXCLUDED.ataque,
              defensa = EXCLUDED.defensa,
              pase = EXCLUDED.pase,
              porteria = EXCLUDED.porteria,
              condicion = EXCLUDED.condicion
            """,
            jugador_vals)

    # 4) Cargar users.json -> usuarios + alineaciones + plantillas
    users = load_json(FILES["users"])
    usuarios_arr = users.get("usuarios", [])
    user_vals = []
    for u in usuarios_arr:
        user_vals.append((
            u.get("id"),
            u.get("tipo"),
            u.get("email"),
            u.get("password"),
            u.get("saldo"),
            u.get("equipo")
        ))
    if user_vals:
        execute_values(cur,
            """
            INSERT INTO usuarios (id,tipo,email,password,saldo,equipo_id)
            VALUES %s
            ON CONFLICT (id) DO UPDATE SET
              tipo = EXCLUDED.tipo,
              email = EXCLUDED.email,
              password = EXCLUDED.password,
              saldo = EXCLUDED.saldo,
              equipo_id = EXCLUDED.equipo_id
            """,
            user_vals)

    # alineaciones y plantillas
    for u in usuarios_arr:
        uid = u.get("id")
        aline = u.get("alineacion")
        if aline:
            cur.execute("""
                INSERT INTO alineaciones (usuario_id, formacion, portero)
                VALUES (%s,%s,%s) ON CONFLICT (usuario_id) DO UPDATE SET formacion=EXCLUDED.formacion, portero=EXCLUDED.portero
            """, (uid, aline.get("formacion"), aline.get("portero")))
            # defensas
            for idx, pid in enumerate(aline.get("defensas", []), start=1):
                cur.execute("""
                    INSERT INTO alineacion_posiciones (usuario_id,posicion_tipo,orden,jugador_id)
                    VALUES (%s,'DEFENSA',%s,%s)
                    ON CONFLICT (usuario_id,posicion_tipo,orden) DO UPDATE SET jugador_id = EXCLUDED.jugador_id
                """, (uid, idx, pid))
            for idx, pid in enumerate(aline.get("medios", []), start=1):
                cur.execute("""
                    INSERT INTO alineacion_posiciones (usuario_id,posicion_tipo,orden,jugador_id)
                    VALUES (%s,'MEDIO',%s,%s)
                    ON CONFLICT (usuario_id,posicion_tipo,orden) DO UPDATE SET jugador_id = EXCLUDED.jugador_id
                """, (uid, idx, pid))
            for idx, pid in enumerate(aline.get("delanteros", []), start=1):
                cur.execute("""
                    INSERT INTO alineacion_posiciones (usuario_id,posicion_tipo,orden,jugador_id)
                    VALUES (%s,'DELANTERO',%s,%s)
                    ON CONFLICT (usuario_id,posicion_tipo,orden) DO UPDATE SET jugador_id = EXCLUDED.jugador_id
                """, (uid, idx, pid))
        # plantillas (array opcional)
        for pid in u.get("plantilla", []):
            cur.execute("""
                INSERT INTO plantillas (usuario_id, jugador_id) VALUES (%s,%s)
                ON CONFLICT (usuario_id, jugador_id) DO NOTHING
            """, (uid, pid))

    # 5) Cargar market.json -> mercado
    market = load_json(FILES["market"])
    market_list = market.get("mercado", [])
    market_vals = []
    for m in market_list:
        market_vals.append((m.get("id"), m.get("jugadorId"), m.get("precioSalida"), m.get("vendedor")))
    if market_vals:
        execute_values(cur,
            """
            INSERT INTO mercado (id,jugador_id,precio_salida,vendedor) VALUES %s
            ON CONFLICT (id) DO UPDATE SET jugador_id = EXCLUDED.jugador_id, precio_salida = EXCLUDED.precio_salida, vendedor = EXCLUDED.vendedor
            """,
            market_vals)

    # 6) Cargar competicion.json -> jornadas, partidos, partido_equipo, goles
    compet = load_json(FILES["competicion"])
    # compet expected: array of jornadas
    for jornada in compet:
        num = jornada.get("numJornada")
        # crear jornada o recuperar id
        cur.execute("INSERT INTO jornadas (num_jornada) VALUES (%s) ON CONFLICT (num_jornada) DO NOTHING", (num,))
        cur.execute("SELECT id FROM jornadas WHERE num_jornada = %s", (num,))
        jornada_id = cur.fetchone()[0]
        for partido in jornada.get("partidos", []):
            el = partido.get("equipoLocal")
            ev = partido.get("equipoVisitante")
            # Asegurar equipos (puede que ya estén)
            if el:
                cur.execute("INSERT INTO equipos (id,nombre) VALUES (%s,%s) ON CONFLICT (id) DO UPDATE SET nombre = EXCLUDED.nombre", (el.get("id"), el.get("nombre")))
            if ev:
                cur.execute("INSERT INTO equipos (id,nombre) VALUES (%s,%s) ON CONFLICT (id) DO UPDATE SET nombre = EXCLUDED.nombre", (ev.get("id"), ev.get("nombre")))
            goles_local = partido.get("golesLocal")
            goles_visit = partido.get("golesVisitante")
            # crear partido
            cur.execute("""
                INSERT INTO partidos (jornada_id, equipo_local, equipo_visitante, goles_local, goles_visitante)
                VALUES (%s,%s,%s,%s,%s) RETURNING id
            """, (jornada_id, el.get("id") if el else None, ev.get("id") if ev else None, goles_local, goles_visit))
            partido_id = cur.fetchone()[0]
            # insertar snapshot para local y visitante
            if el:
                cur.execute("""
                    INSERT INTO partido_equipo (partido_id,equipo_id,es_local,partidos_jugados,victorias,empates,derrotas,goles_favor,goles_contra,puntos)
                    VALUES (%s,%s,true,%s,%s,%s,%s,%s,%s,%s)
                    ON CONFLICT (partido_id,equipo_id) DO NOTHING
                """, (partido_id, el.get("id"), el.get("partidosJugados"), el.get("victorias"), el.get("empates"), el.get("derrotas"), el.get("golesFavor"), el.get("golesContra"), el.get("puntos")))
            if ev:
                cur.execute("""
                    INSERT INTO partido_equipo (partido_id,equipo_id,es_local,partidos_jugados,victorias,empates,derrotas,goles_favor,goles_contra,puntos)
                    VALUES (%s,%s,false,%s,%s,%s,%s,%s,%s,%s)
                    ON CONFLICT (partido_id,equipo_id) DO NOTHING
                """, (partido_id, ev.get("id"), ev.get("partidosJugados"), ev.get("victorias"), ev.get("empates"), ev.get("derrotas"), ev.get("golesFavor"), ev.get("golesContra"), ev.get("puntos")))

            # procesar goles: cada gol tiene un objeto "jugador" y "minuto"
            for g in partido.get("goles", []):
                jug = g.get("jugador")
                minuto = g.get("minuto")
                if not jug:
                    continue
                # upsert jugador (porque competicion.json incluye objeto jugador completo)
                cur.execute("""
                    INSERT INTO jugadores (id,nombre,posicion,equipo_id,precio,ataque,defensa,pase,porteria,condicion)
                    VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)
                    ON CONFLICT (id) DO UPDATE SET
                      nombre = EXCLUDED.nombre,
                      posicion = EXCLUDED.posicion,
                      equipo_id = EXCLUDED.equipo_id,
                      precio = EXCLUDED.precio,
                      ataque = EXCLUDED.ataque,
                      defensa = EXCLUDED.defensa,
                      pase = EXCLUDED.pase,
                      porteria = EXCLUDED.porteria,
                      condicion = EXCLUDED.condicion
                """, (jug.get("id"), jug.get("nombre"), jug.get("posicion"), jug.get("equipoId"),
                      jug.get("precio"), jug.get("ataque"), jug.get("defensa"),
                      jug.get("pase"), jug.get("porteria"), jug.get("condition")))
                # insertar gol
                cur.execute("INSERT INTO goles (partido_id, jugador_id, minuto, equipo_id) VALUES (%s,%s,%s,%s)",
                            (partido_id, jug.get("id"), minuto, jug.get("equipoId")))

    cur.close()
    conn.close()
    print("Importación completada correctamente.")

if __name__ == "__main__":
    main()
