1. Tabla: USERS
Campo	Tipo	PK	FK	Descripción
id	VARCHAR	✔		ID del usuario (ej: U0001)
tipo	VARCHAR			admin / estándar
email	VARCHAR			correo único
password	VARCHAR			hash
saldo	DECIMAL			
equipo	VARCHAR		✔ teams(id)	equipo favorito (solo admin en tu JSON pero se puede generalizar)
2. Tabla: ALIGNMENTS (alineación del usuario)

Una alineación por usuario.

Campo	Tipo	PK	FK	Descripción
id	INT AUTO	✔		PK de alineación
user_id	VARCHAR	✔	users(id)	relación 1–1
formacion	VARCHAR			(ej: 4-4-2)
portero	VARCHAR		✔ players(id)	portero titular
3. Tabla: ALIGNMENT_POSITIONS (listas de defensas, medios, delanteros)

Relación 1-N para almacenar todos los jugadores alineados.

Campo	Tipo	PK	FK	Descripción
id	INT AUTO	✔		
alignment_id	INT		✔ alignments(id)	alineación a la que pertenece
posicion	ENUM('DEFENSA','MEDIO','DELANTERO')			tipo
jugador_id	VARCHAR		✔ players(id)	jugador
4. Tabla: TEAMS
Campo	Tipo	PK	FK	Descripción
id	VARCHAR	✔		(ej: T01)
nombre	VARCHAR			nombre del club
temporada	VARCHAR			2025/26
liga	VARCHAR			LALIGA EA SPORTS
5. Tabla: PLAYERS
Campo	Tipo	PK	FK
id	VARCHAR	✔	
nombre	VARCHAR		
posicion	VARCHAR		
equipoId	VARCHAR		✔ teams(id)
precio	DECIMAL		
ataque	INT		
defensa	INT		
pase	INT		
porteria	INT		
condition	INT		
6. Tabla: MARKET
Campo	Tipo	PK	FK
id	VARCHAR	✔	
jugadorId	VARCHAR		✔ players(id)
precioSalida	DECIMAL		
vendedor	VARCHAR		✔ users(id)
