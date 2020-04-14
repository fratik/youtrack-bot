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

import io.undertow.predicate.Predicates;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.accesslog.AccessLogHandler;
import io.undertow.server.handlers.accesslog.AccessLogReceiver;
import io.undertow.server.handlers.encoding.ContentEncodingRepository;
import io.undertow.server.handlers.encoding.EncodingHandler;
import io.undertow.server.handlers.encoding.GzipEncodingProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomHandlers {
    private CustomHandlers() {}

    private static AccessLogHandler accessLog(HttpHandler next, Logger logger) {
        return new AccessLogHandler(next, new Slf4jAccessLogReceiver(logger), "%{REMOTE_HOST_PROXIED} %l " +
                "%u %t \"%r\" %s %b \"%{i,Referer}\" \"%{i,User-Agent}\"",
                CustomHandlers.class.getClassLoader());
    }

    public static AccessLogHandler accessLog(HttpHandler next) {
        final Logger logger = LoggerFactory.getLogger(AccessLogHandler.class);
        return accessLog(next, logger);
    }

    public static HttpHandler gzip(HttpHandler next) {
        return new EncodingHandler(new ContentEncodingRepository()
                .addEncodingHandler("gzip",
                        // This 1000 is a priority, not exactly sure what it does.
                        new GzipEncodingProvider(), 1000,
                        // Anything under a content-length of 20 will not be gzipped
//                        Predicates.truePredicate(),
                        Predicates.maxContentSize(20) // https://issues.jboss.org/browse/UNDERTOW-1234
                ))
                .setNext(next);
    }

    public static class Slf4jAccessLogReceiver implements AccessLogReceiver {
        private final Logger logger;

        public Slf4jAccessLogReceiver(final Logger logger) {
            this.logger = logger;
        }

        @Override
        public void logMessage(String message) {
            logger.info(message);
        }
    }
}