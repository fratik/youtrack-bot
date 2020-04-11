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

package pl.fratik.youtrackbot.api.models;

import pl.fratik.youtrackbot.api.exceptions.APIException;
import pl.fratik.youtrackbot.api.exceptions.UnauthorizedException;

import java.util.List;

/**
 * Projekt
 */
public interface Project {
    /**
     * Zwraca nazwę projektu
     * @return nazwa projektu
     */
    String getName();

    /**
     * Zwraca wewnętrzne ID projektu
     * @return wewnętrzne ID projektu
     */
    String getId();

    /**
     * Zwraca krótką nazwę (ID) projektu
     * @return krótka nazwa (ID) projektu
     */
    String getShortName();

    /**
     * Pobiera zgłoszenia tego projektu z serwera
     * @return pobrane zgłoszenia
     * @throws UnauthorizedException w przypadku braku dostępu
     * @throws APIException jeżeli połączenie się nie uda
     */
    List<Issue> retrieveIssues() throws APIException;
}
