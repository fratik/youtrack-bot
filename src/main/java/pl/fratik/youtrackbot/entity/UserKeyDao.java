/*
 * Copyright (C) 2020 YouTrack Bot Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * YouTrack is a product released by JetBrains s.r.o.
 * and this project is not affiliated with them.
 */

package pl.fratik.youtrackbot.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import gg.amy.pgorm.PgMapper;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.LoggerFactory;

import java.util.List;

public class UserKeyDao implements Dao<UserKey> {

    private final PgMapper<UserKey> mapper;

    public UserKeyDao(ManagerBazyDanych managerBazyDanych) {
        if (managerBazyDanych == null) throw new IllegalStateException("managerBazyDanych == null");
        mapper = managerBazyDanych.getPgStore().mapSync(UserKey.class);
    }

    @Override
    public UserKey get(String id) {
        return mapper.load(id).orElseGet(() -> newObject(id));
    }

    public UserKey get(User user) {
        return get(user.getId());
    }

    @Override
    public List<UserKey> getAll() {
        return mapper.loadAll();
    }

    @Override
    public void save(UserKey toCos) {
        ObjectMapper objMapper = new ObjectMapper();
        String jsoned;
        try { jsoned = objMapper.writeValueAsString(toCos); } catch (Exception ignored) { jsoned = toCos.toString(); }
        LoggerFactory.getLogger(getClass()).debug("Zmiana danych w DB: {} -> {} -> {}", toCos.getTableName(),
                toCos.getClass().getName(), jsoned);
        mapper.save(toCos);
    }

    private UserKey newObject(String id) {
        return new UserKey(id);
    }

}
