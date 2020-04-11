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

package pl.fratik.youtrackbot.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import pl.fratik.youtrackbot.Ustawienia;
import pl.fratik.youtrackbot.api.exceptions.APIException;
import pl.fratik.youtrackbot.api.exceptions.UnauthorizedException;
import pl.fratik.youtrackbot.api.models.Issue;
import pl.fratik.youtrackbot.api.models.Project;
import pl.fratik.youtrackbot.api.models.deserializer.FieldDeserializer;
import pl.fratik.youtrackbot.api.models.impl.IssueImpl;
import pl.fratik.youtrackbot.api.models.impl.ProjectImpl;
import pl.fratik.youtrackbot.api.models.impl.ProjectJson;
import pl.fratik.youtrackbot.util.JSONResponse;
import pl.fratik.youtrackbot.util.JSONResponseArray;
import pl.fratik.youtrackbot.util.JSONResponseObject;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static pl.fratik.youtrackbot.util.NetworkUtil.encodeURIComponent;

public class YouTrack {
    private static final String USER_AGENT = "YouTrack Bot/1.0.0";
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new GsonBuilder().registerTypeAdapter(IssueImpl.FieldImpl.class,
            new FieldDeserializer()).disableHtmlEscaping().create();

    private final String apiUrl;
    private final String clientId;
    private final String clientSecret;
    private final String scope;
    private String tokenType;
    private String token;
    private long expiresOn;

    public YouTrack(String apiUrl, String clientId, String clientSecret, String scope) throws APIException {
        this.apiUrl = apiUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.scope = scope;
        refreshToken();
    }

    /**
     * Odświeża token
     * @throws UnauthorizedException w przypadku błędu 401
     * @throws APIException kiedy nie uda się połączyć
     */
    private void refreshToken() throws APIException {
        try {
            String auth = "Basic " + Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());
            Response res = makeRequest(Ustawienia.instance.hubUrl + "/api/rest/oauth2/token", auth,
                    MediaType.get("application/x-www-form-urlencoded"), "grant_type=client_credentials&scope="
                            + scope);
            JSONResponseObject json = toJsonResponseObject(res);
            if (json == null || json.getCode() != 200) throw new UnauthorizedException("Błąd logowania: " + json);
            tokenType = json.getString("token_type");
            token = json.getString("access_token");
            expiresOn = Instant.now().toEpochMilli() + (json.getInt("expires_in") * 1000);
        } catch (IOException e) {
            throw new APIException(e);
        }
    }

    private JSONResponseObject toJsonResponseObject(Response res) throws IOException {
        return res.body() == null ? null : new JSONResponseObject(res.body().string(), res.code());
    }

    private JSONResponseArray toJsonResponseArray(Response res) throws IOException {
        return res.body() == null ? null : new JSONResponseArray(res.body().string(), res.code());
    }

    /**
     * Zwraca token w formacie do wykonywania żądań
     * @return token w formacie "TokenType Token", np. Bearer 123456789
     * @throws UnauthorizedException w przypadku błędu 401
     * @throws APIException przy błędzie połączenia
     */
    private String getToken() throws APIException {
        try {
            if (expiresOn - Instant.now().toEpochMilli() >= (300 * 1000))
                refreshToken();
            return tokenType + " " + token;
        } catch (IOException e) {
            throw new APIException(e);
        }
    }

    /**
     * Zwraca listę projektów
     * @return lista projektów
     * @throws UnauthorizedException w przypadku błędu 401
     * @throws APIException przy błędzie połączenia
     */
    public List<Project> getProjects() throws APIException {
        String base = apiUrl + "/admin/projects?fields=" + encodeURIComponent(ProjectImpl.FIELDS);
        List<ProjectJson> pjs = requestPaginatedList(base, new TypeToken<ProjectJson>(){});
        List<Project> res = new ArrayList<>();
        for (ProjectJson p : pjs) res.add(new ProjectImpl(p.getName(), p.getId(), p.getShortName(), this));
        return res;
    }

    /**
     * Zwraca pełną listę zgłoszeń
     * @return pełna lista zgłoszeń
     * @throws UnauthorizedException w przypadku błędu 401
     * @throws APIException przy błędzie połączenia
     */
    public List<Issue> getIssues() throws APIException {
        return getIssues((String) null);
    }

    /**
     * Zwraca listę zgłoszeń pasującą do zapytania
     * @param query Zapytanie
     * @return lista zgłoszeń pasująca do zapytania
     * @throws UnauthorizedException w przypadku błędu 401
     * @throws APIException przy błędzie połączenia
     */
    public List<Issue> getIssues(String query) throws APIException {
        String base = apiUrl + "/issues?fields=" + encodeURIComponent(IssueImpl.FIELDS);
        if (query != null) base = base + "&query=" + encodeURIComponent(query);
        return requestPaginatedList(base, new TypeToken<IssueImpl>(){});
    }

    /**
     * Zwraca listę zgłoszeń dla konkretnego projektu
     * @param project Projekt
     * @return lista zgłoszeń dla projektu
     * @throws UnauthorizedException w przypadku błędu 401
     * @throws APIException przy błędzie połączenia
     */
    public List<Issue> getIssues(Project project) throws APIException {
        String base = apiUrl + "/admin/projects/" + project.getId() + "/issues?fields=" +
                encodeURIComponent(IssueImpl.FIELDS);
        return requestPaginatedList(base, new TypeToken<IssueImpl>(){});
    }

    @NotNull
    private <T> List<T> requestPaginatedList(String baseUrl, TypeToken<? extends T> typeToken) throws APIException {
        List<T> list = new ArrayList<>();
        boolean paginate;
        int skip = 0;
        int top = 50;
        do {
            try {
                Response resp = makeRequest(baseUrl + "&$top=" + top + "&$skip=" + skip, getToken());
                JSONResponseArray json = toJsonResponseArray(resp);
                checkJsonResponse(json);
                List<T> lista = gson.fromJson(json.toString(),
                        TypeToken.getParameterized(TypeToken.get(List.class).getType(), typeToken.getType()).getType());
                if (lista.size() == top) {
                    skip += top;
                    paginate = true;
                } else {
                    paginate = false;
                }
                list.addAll(lista);
            } catch (IOException e) {
                throw new APIException(e);
            }
        } while (paginate);
        return list;
    }

    private void checkJsonResponse(JSONResponse json) throws IOException {
        if (json == null) throw new IOException("json == null");
        if (json.getCode() == 401 || json.getCode() == 403) throw new UnauthorizedException("kod 401 lub 403");
        if (json.getCode() != 200) throw new APIException("kod nie 200");
    }

    private Response makeRequest(String url, String authorization) throws IOException {
        return makeRequest(url, authorization, null, null);
    }

    private Response makeRequest(String url, String authorization, MediaType type, String content) throws IOException {
        Request.Builder reqbd = new Request.Builder()
                .header("User-Agent", USER_AGENT)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .url(url);
        if (authorization != null) reqbd.header("Authorization", authorization);
        if (type != null && content != null) reqbd = reqbd.post(RequestBody.create(type, content));
        return client.newCall(reqbd.build()).execute();
    }
}
