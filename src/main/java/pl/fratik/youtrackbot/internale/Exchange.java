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

package pl.fratik.youtrackbot.internale;

import com.google.gson.Gson;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

import java.nio.ByteBuffer;
import java.util.Deque;
import java.util.Optional;

@SuppressWarnings("all")
public class Exchange {
    private Exchange() {}

    private static final BodyImpl BODY = new BodyImpl();
    public static BodyImpl body() {
        return BODY;
    }

    private static final QueryParamImpl QUERYPARAMS = new QueryParamImpl(){};
    public static QueryParamImpl queryParams() {
        return QUERYPARAMS;
    }

    public static class BodyImpl {
        public void sendHtml(HttpServerExchange exchange, String html) {
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html");
            exchange.getResponseSender().send(html);
        }
        public void sendJson(HttpServerExchange exchange, Object obj, int code) {
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            exchange.setStatusCode(code);
            exchange.getResponseSender().send(ByteBuffer.wrap(new Gson().toJson(obj).getBytes()));
        }
    }

    public static class QueryParamImpl {
        public Optional<String> queryParam(HttpServerExchange exchange, String name) {
            return Optional.ofNullable(exchange.getQueryParameters().get(name))
                    .map(Deque::getFirst);
        }
    }
}
