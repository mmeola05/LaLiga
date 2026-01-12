package com.liga.repository;

import com.liga.repository.dao.EquipoDAO;
import com.liga.repository.dao.JorandaDAO;
import com.liga.repository.dao.JugadorDAO;
import com.liga.repository.dao.MarketDAO;
import com.liga.repository.dao.UsersDAO;
import com.liga.repository.json.EquipoDAOImplJSON;
import com.liga.repository.json.JornadaDAOImplJSON;
import com.liga.repository.json.JugadorDAOImplJSON;
import com.liga.repository.json.MarketDAOImplJSON;
import com.liga.repository.json.UsersDAOImplJSON;
import com.liga.repository.postgres.*;

public class RepositoryFactory {
    private static LeagueRepository leagueRepository;

    public static LeagueRepository create(Backend backend) {
        // Singleton sencillo: si ya existe, lo devuelve.
        // NOTA: Si quisiéramos soportar cambio en caliente, habría que resetearlo.
        if (leagueRepository == null) {
            switch (backend) {
                case JSON -> {
                    EquipoDAO equipoDAO = new EquipoDAOImplJSON();
                    JugadorDAO jugadorDAO = new JugadorDAOImplJSON();
                    MarketDAO marketDAO = new MarketDAOImplJSON();
                    UsersDAO usersDAO = new UsersDAOImplJSON();
                    JorandaDAO jornadaDAO = new JornadaDAOImplJSON();
                    leagueRepository = new LeagueRepositoryImpl(equipoDAO, jugadorDAO, marketDAO, usersDAO, jornadaDAO);
                }
                case DB -> {
                    EquipoDAO equipoDAO = new EquipoDAOImplPostgres();
                    JugadorDAO jugadorDAO = new JugadorDAOImplPostgres();
                    MarketDAO marketDAO = new MarketDAOImplPostgres();
                    UsersDAO usersDAO = new UsersDAOImplPostgres();
                    JorandaDAO jornadaDAO = new JornadaDAOImplPostgres();
                    leagueRepository = new LeagueRepositoryImpl(equipoDAO, jugadorDAO, marketDAO, usersDAO, jornadaDAO);
                }
                default -> throw new IllegalArgumentException("Backend no soportado: " + backend);
            }
        }
        return leagueRepository;
    }

    // Deprecated alias for compatibility until refactor is complete
    public static LeagueRepository getLeagueRepository() {
        return create(Backend.JSON);
    }
}
