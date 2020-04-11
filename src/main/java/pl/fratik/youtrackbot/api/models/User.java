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

import lombok.SneakyThrows;
import okhttp3.Response;
import pl.fratik.youtrackbot.util.NetworkUtil;

/**
 * Użytkownik
 */
public interface User {
    /**
     * Zwraca login użytkownika
     * @return login użytkownika
     */
    String getLogin();

    /**
     * Zwraca pełną nazwę użytkownika
     * @return pełna nazwa użytkownika
     */
    String getFullName();

    /**
     * Zwraca ID użytkownika z huba
     * @return ID użytkownika z huba
     */
    String getRingId();

    /**
     * Zwraca rozwiązany link do avataru
     * @return rozwiązany link do avataru, null jeśli nie jpg/png
     */
    @SneakyThrows
    default String getAvatarUrl() {
        Response resp = NetworkUtil.headRequestNoRedirect(getAvatarUrlRaw());
        String location = resp.header("Location");
        String content = resp.header("Content-Type");
        String url;
        if (location != null) {
            url = location;
            content = NetworkUtil.headRequestNoRedirect(url).header("Content-Type");
        }
        else url = getAvatarUrlRaw();
        if (content != null && (content.contains("image/png") || content.contains("image/jpg"))) return url;
        else return null;
    }

    /**
     * Zwraca link do avataru
     * @return link do avataru
     */
    String getAvatarUrlRaw();
}
