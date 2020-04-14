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

package pl.fratik.youtrackbot.api.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import pl.fratik.youtrackbot.api.exceptions.APIException;
import pl.fratik.youtrackbot.api.exceptions.UnauthorizedException;

import java.time.Instant;

public interface Auth {
    /**
     * Odświeża token
     * @return Typ token'u (Bearer), sam token, Instant wygaśnięcia
     * @throws UnauthorizedException w przypadku błędu 401
     * @throws APIException kiedy nie uda się połączyć
     */
    TokenResponse refreshToken() throws APIException;

    @Data
    @AllArgsConstructor
    class TokenResponse {
        private final String tokenType;
        private final String token;
        private final Instant expiresOn;
    }
}
