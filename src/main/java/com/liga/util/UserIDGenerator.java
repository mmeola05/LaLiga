package com.liga.util;

import com.liga.repository.dao.UsersDAO;
import com.liga.repository.file.UsersDAOImplJSON;

public class UserIDGenerator {

    public static String nextId() {
        UsersDAO dao = new UsersDAOImplJSON();

        int max = dao.findAll()
                .stream()
                .map(u -> u.getId())
                .map(id -> id.substring(1)) // quitar 'U'
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(0);

        int next = max + 1;

        return String.format("U%04d", next);
    }
}
