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

import lombok.Setter;
import okhttp3.MediaType;
import okhttp3.Response;
import pl.fratik.youtrackbot.Ustawienia;
import pl.fratik.youtrackbot.api.YouTrack;
import pl.fratik.youtrackbot.api.exceptions.APIException;
import pl.fratik.youtrackbot.api.exceptions.UnauthorizedException;
import pl.fratik.youtrackbot.util.JSONResponseObject;

import java.io.IOException;
import java.time.Instant;
import java.util.Base64;

public class OAuth2RefreshToken implements Auth {
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    @Setter private String refreshToken;

    public OAuth2RefreshToken(String clientId, String clientSecret, String refreshToken, String redirectUri) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.refreshToken = refreshToken;
        this.redirectUri = redirectUri;
    }

    @Override
    public TokenResponse refreshToken() throws APIException {
        try {
            String auth = "Basic " + Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());
            Response res = YouTrack.makeRequest(Ustawienia.instance.hubUrl + "/api/rest/oauth2/token", auth,
                    MediaType.get("application/x-www-form-urlencoded"), "grant_type=refresh_token&refresh_token="
                            + refreshToken);
            JSONResponseObject json = YouTrack.toJsonResponseObject(res);
            if (json == null || json.getCode() != 200) throw new UnauthorizedException("Błąd logowania: ", json);
            Instant expiresOn = Instant.ofEpochMilli(Instant.now().toEpochMilli() + (json.getInt("expires_in") * 1000));
            return new TokenResponse(json.getString("token_type"), json.getString("access_token"), expiresOn);
        } catch (APIException e) {
            throw e;
        } catch (IOException e) {
            throw new APIException(e, null);
        }
    }

    public String getRefreshToken(String code) throws APIException {
        try {
            String auth = "Basic " + Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());
            Response res = YouTrack.makeRequest(Ustawienia.instance.hubUrl + "/api/rest/oauth2/token", auth,
                    MediaType.get("application/x-www-form-urlencoded"), "grant_type=authorization_code&code="
                            + code + "&redirect_uri=" + redirectUri);
            JSONResponseObject json = YouTrack.toJsonResponseObject(res);
            if (json == null || json.getCode() != 200) throw new UnauthorizedException("Błąd logowania: ", json);
            return json.getString("refresh_token");
        } catch (APIException e) {
            throw e;
        } catch (IOException e) {
            throw new APIException(e, null);
        }
    }
}
