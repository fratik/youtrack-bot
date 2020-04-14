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
import org.json.JSONArray;
import org.json.JSONObject;
import pl.fratik.youtrackbot.Ustawienia;
import pl.fratik.youtrackbot.api.auth.Auth;
import pl.fratik.youtrackbot.api.exceptions.APIException;
import pl.fratik.youtrackbot.api.exceptions.UnauthorizedException;
import pl.fratik.youtrackbot.api.models.Issue;
import pl.fratik.youtrackbot.api.models.Project;
import pl.fratik.youtrackbot.api.models.User;
import pl.fratik.youtrackbot.api.models.deserializer.FieldDeserializer;
import pl.fratik.youtrackbot.api.models.impl.IssueImpl;
import pl.fratik.youtrackbot.api.models.impl.ProjectImpl;
import pl.fratik.youtrackbot.api.models.impl.ProjectJson;
import pl.fratik.youtrackbot.api.models.impl.UserImpl;
import pl.fratik.youtrackbot.util.JSONResponse;
import pl.fratik.youtrackbot.util.JSONResponseArray;
import pl.fratik.youtrackbot.util.JSONResponseObject;

import java.io.IOException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import static pl.fratik.youtrackbot.util.NetworkUtil.encodeURIComponent;

public class YouTrack {
    private static final String USER_AGENT = "YouTrack Bot/1.0.0";
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new GsonBuilder().registerTypeAdapter(IssueImpl.FieldImpl.class,
            new FieldDeserializer()).disableHtmlEscaping().create();
    public static final Pattern ISSUE_ID = Pattern.compile("([A-Z0-9]+)-(\\d+)");

    private final String apiUrl;
    private final Auth auth;
    private String tokenType;
    private String token;
    private Instant expiresOn;

    public YouTrack(String apiUrl, Auth auth) throws APIException {
        this.apiUrl = apiUrl + "/api";
        this.auth = auth;
        refreshToken();
    }

    public static JSONResponseObject toJsonResponseObject(Response res) throws IOException {
        return res.body() == null ? null : new JSONResponseObject(res.body().string(), res.code());
    }

    private JSONResponseArray toJsonResponseArray(Response res) throws IOException {
        return res.body() == null ? null : new JSONResponseArray(res.body().string(), res.code());
    }

    private void refreshToken() throws APIException {
        Auth.TokenResponse tr = auth.refreshToken();
        tokenType = tr.getTokenType();
        token = tr.getToken();
        expiresOn = tr.getExpiresOn();
    }

    /**
     * Zwraca token w formacie do wykonywania żądań
     * @return token w formacie "TokenType Token", np. Bearer 123456789
     * @throws UnauthorizedException w przypadku błędu 401
     * @throws APIException przy błędzie połączenia
     */
    private String getToken() throws APIException {
        try {
            if (expiresOn.toEpochMilli() - Instant.now().toEpochMilli() >= (300 * 1000))
                refreshToken();
            return tokenType + " " + token;
        } catch (IOException e) {
            throw new APIException(e, null);
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

    /**
     * Zwraca dane o użytkowniku wykonującym
     * @return dane o wykonującym
     * @throws UnauthorizedException w przypadku błędu 401
     * @throws APIException przy błędzie połączenia
     */
    public User getMe() throws APIException {
        return request(apiUrl + "/users/me?fields=" + encodeURIComponent(UserImpl.FIELDS),
                new TypeToken<UserImpl>(){});
    }

    private <T> T request(String url, TypeToken<? extends T> typeToken) throws APIException {
        try {
            Response resp = makeRequest(url, getToken());
            JSONResponseObject json = toJsonResponseObject(resp);
            checkJsonResponse(json);
            return gson.fromJson(json.toString(), typeToken.getType());
        } catch (IOException e) {
            throw new APIException(e, null);
        }
    }

    private <T> T request(String url, TypeToken<? extends T> typeToken, MediaType type, String content) throws APIException {
        try {
            Response resp = makeRequest(url, getToken(), type, content);
            JSONResponseObject json = toJsonResponseObject(resp);
            checkJsonResponse(json);
            return gson.fromJson(json.toString(), typeToken.getType());
        } catch (IOException e) {
            throw new APIException(e, null);
        }
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
                throw new APIException(e, null);
            }
        } while (paginate);
        try {
            Method method;
            Class<?> clazz = typeToken.getRawType();
            method = clazz.getDeclaredMethod("setYouTrack", YouTrack.class);
            for (T el : list)
                method.invoke(el, this);
        } catch (Exception ignored) {}
        return list;
    }

    private void checkJsonResponse(JSONResponse json) throws IOException {
        if (json == null) throw new IOException("json == null");
        if (json.getCode() == 401 || json.getCode() == 403) throw new UnauthorizedException("kod 401 lub 403", json);
        if (json.getCode() != 200) throw new APIException("kod nie 200", json);
    }

    public static Response makeRequest(String url, String authorization) throws IOException {
        return makeRequest(url, authorization, null, null);
    }

    public static Response makeRequest(String url, String authorization, MediaType type, String content) throws IOException {
        Request.Builder reqbd = new Request.Builder()
                .header("User-Agent", USER_AGENT)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .url(url);
        if (authorization != null) reqbd.header("Authorization", authorization);
        if (type != null && content != null) reqbd = reqbd.post(RequestBody.create(type, content));
        return client.newCall(reqbd.build()).execute();
    }

    public static String getOAuth2AuthUrl() {
        return "https://hub.fratikbot.pl/hub/api/rest/oauth2/auth" +
                "?response_type=code&redirect_uri=" + encodeURIComponent(Ustawienia.instance.redirect) +
                "&request_credentials=default&client_id=" + encodeURIComponent(Ustawienia.instance.id) +
                "&access_type=offline&scope=" + encodeURIComponent(Ustawienia.instance.scope);
    }

    public Issue setUserField(Issue issue, Issue.Field field, User user) throws APIException {
        JSONObject root = new JSONObject();
        JSONObject fieldJ = new JSONObject();
        fieldJ.put("id", field.getId());
        fieldJ.put("$type", field.get$type());
        if (!Objects.equals(field.get$type(), "SingleUserIssueCustomField"))
            throw new IllegalArgumentException("$typ to nie SingleUserIssueCustomField");
        fieldJ.put("value", new JSONObject().put("login", user.getLogin()));
        root.put("customFields", new JSONArray().put(fieldJ));
        return request(apiUrl + "/issues/" + issue.getId() + "?fields=" + encodeURIComponent(IssueImpl.FIELDS),
                new TypeToken<IssueImpl>(){}, MediaType.get("application/json"), root.toString());
    }
}
