package com.liga.repository;

import com.liga.repository.dao.EquipoDAO;
import com.liga.repository.dao.JorandaDAO;
import com.liga.repository.dao.JugadorDAO;
import com.liga.repository.dao.MarketDAO;
import com.liga.repository.dao.UsersDAO;
import com.liga.repository.file.EquipoDAOImplJSON;
import com.liga.repository.file.JornadaDAOImplJSON;
import com.liga.repository.file.JugadorDAOImplJSON;
import com.liga.repository.file.MarketDAOImplJSON;
import com.liga.repository.file.UsersDAOImplJSON;
import com.liga.repository.postgres.EquipoDAOImplPostgres;
import com.liga.repository.postgres.UsersDAOImplPostgres;

public class RepositoryFactory {
  private static LeagueRepository leagueRepository;

  public static LeagueRepository getLeagueRepository() {
    if (leagueRepository == null) {
      //EquipoDAO equipoDAO = new EquipoDAOImplJSON();

        EquipoDAO equipoDAO = new EquipoDAOImplPostgres();
      JugadorDAO jugadorDAO = new JugadorDAOImplJSON();
      MarketDAO marketDAO = new MarketDAOImplJSON();
      //UsersDAO usersDAO = new UsersDAOImplJSON();
        UsersDAO usersDAO = new UsersDAOImplPostgres();
      JorandaDAO jornadaDAO = new JornadaDAOImplJSON();

      leagueRepository = new LeagueRepositoryImpl(equipoDAO, jugadorDAO, marketDAO, usersDAO, jornadaDAO);
    }
    return leagueRepository;
  }

}
