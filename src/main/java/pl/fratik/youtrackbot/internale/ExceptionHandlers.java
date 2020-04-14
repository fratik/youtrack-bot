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

import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.ExceptionHandler;
import lombok.Data;
import org.slf4j.LoggerFactory;

public class ExceptionHandlers {

    private ExceptionHandlers() {}

    public static void handleAllExceptions(HttpServerExchange exchange) {
        LoggerFactory.getLogger(ExceptionHandlers.class).error("Błąd w request'cie!",
                exchange.getAttachment(ExceptionHandler.THROWABLE));
        Exchange.body().sendJson(exchange, new GenericException("Internal Server Error!"), 500);
    }

    @Data
    public static class GenericException {
        private final String error;
        private final boolean success = false;
        public GenericException(String errorMessage) {
            error = errorMessage;
        }
    }

}
