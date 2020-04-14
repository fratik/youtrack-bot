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

package pl.fratik.youtrackbot;

import com.google.common.eventbus.AsyncEventBus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.BlockingHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.exceptions.ContextException;
import net.dv8tion.jda.api.requests.RestAction;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.fratik.youtrackbot.api.YouTrack;
import pl.fratik.youtrackbot.api.auth.ClientCredentials;
import pl.fratik.youtrackbot.commands.Command;
import pl.fratik.youtrackbot.commands.CommandManager;
import pl.fratik.youtrackbot.commands.TesterCommand;
import pl.fratik.youtrackbot.entity.ManagerBazyDanych;
import pl.fratik.youtrackbot.entity.UserKeyDao;
import pl.fratik.youtrackbot.internale.CustomHandlers;
import pl.fratik.youtrackbot.internale.ExceptionHandlers;
import pl.fratik.youtrackbot.internale.Exchange;
import pl.fratik.youtrackbot.internale.MiddlewareBuilder;
import pl.fratik.youtrackbot.listener.DMListener;
import pl.fratik.youtrackbot.listener.IssueListener;
import pl.fratik.youtrackbot.listener.Listener;
import pl.fratik.youtrackbot.listener.ListenerManager;

import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private JDA jda;
    private ListenerManager listenerManager;
    private CommandManager commandManager;
    private ManagerBazyDanych mbd;
    private Undertow undertow;
    private AsyncEventBus eventBus;

    private static final File cfg = new File("config.json");

    public Main(String token) {
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown, "Shutdown Thread"));
        eventBus = new AsyncEventBus(Executors.newFixedThreadPool(16));
        logger.info("Loguje się...");

        Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().setPrettyPrinting().create();

        if (!cfg.exists()) {
            try {
                if (cfg.createNewFile()) {
                    Ustawienia.instance = new Ustawienia();
                    Files.write(cfg.toPath(), gson.toJson(Ustawienia.instance).getBytes(StandardCharsets.UTF_8));
                    logger.info("Konfiguracja stworzona, ustaw bota!");
                    System.exit(1);
                } else throw new Exception("cfg.createNewFile() == false");
            } catch (Exception e) {
                logger.error("Nie udało się stworzyć konfiguracji!", e);
                System.exit(1);
            }
        }

        try {
            Ustawienia.instance = gson.fromJson(new FileReader(cfg), Ustawienia.class);
        } catch (Exception e) {
            logger.error("Nie udało się odczytać konfiguracji!", e);
            System.exit(1);
        }

        try {
            JDAEventHandler eventHandler = new JDAEventHandler(eventBus);
            JDABuilder builder = new JDABuilder();
            builder.setEnableShutdownHook(false);
            builder.setAutoReconnect(true);
            builder.setToken(token);
            builder.setStatus(OnlineStatus.DO_NOT_DISTURB);
            builder.setActivity(Activity.playing("Ładowanie..."));
            builder.addEventListeners(eventHandler);
            builder.setEnableShutdownHook(false);
            builder.setBulkDeleteSplittingEnabled(false);
            builder.setCallbackPool(Executors.newFixedThreadPool(4));
            jda = builder.build();

            mbd = new ManagerBazyDanych();
            mbd.load();
            UserKeyDao ukd = new UserKeyDao(mbd);
            RoutingHandler routes = new RoutingHandler();
            routes.get("/token", ex -> {
                ClassLoader cl;
                cl = getClass().getClassLoader();
                if (cl == null) cl = ClassLoader.getSystemClassLoader();
                try {
                    String error = Exchange.queryParams().queryParam(ex, "error").orElse(null);
                    if (error != null) throw new Exception(error);
                    String code = Exchange.queryParams().queryParam(ex, "code")
                            .orElseThrow(() -> new Exception("nie ma kodu"));
                    String html = IOUtils.resourceToString("code.success.html", StandardCharsets.UTF_8, cl);
                    Exchange.body().sendHtml(ex, html.replace("{{kot}}", code));
                } catch (Exception e) {
                    String html = IOUtils.resourceToString("code.error.html", StandardCharsets.UTF_8, cl);
                    Exchange.body().sendHtml(ex.setStatusCode(400), html.replace("{{blad}}", e.getMessage()));
                }
            });
            undertow = Undertow.builder()
                    .addHttpListener(Ustawienia.instance.port, Ustawienia.instance.host, wrapWithMiddleware(routes))
                    .build();
            undertow.start();

            YouTrack yt = new YouTrack(Ustawienia.instance.youTrackUrl, new ClientCredentials(Ustawienia.instance.id,
                    Ustawienia.instance.secret, Ustawienia.instance.scope));
            listenerManager = new ListenerManager(eventBus);
            commandManager = new CommandManager(eventBus);

            List<Listener> listeners = new ArrayList<>();
            listeners.add(new IssueListener(yt));
            listeners.add(new DMListener(ukd));
            listenerManager.registerListeners(listeners);
            List<Command> commands = new ArrayList<>();
            commands.add(new TesterCommand(ukd));
            commandManager.registerCommands(commands);
            eventBus.register(commandManager);

            jda.awaitReady();
            jda.getPresence().setStatus(OnlineStatus.ONLINE);
            jda.getPresence().setActivity(Activity.playing("Egzystencja to cierpienie"));
            logger.info("Połączono, zalogowano jako {}!", jda.getSelfUser());
        } catch (Exception e) {
            logger.error("Nie udało się wystartować bota!", e);
            System.exit(2);
        }
    }

    private HttpHandler wrapWithMiddleware(RoutingHandler handler) {
        return MiddlewareBuilder.begin(BlockingHandler::new)
                .next(CustomHandlers::gzip)
                .next(CustomHandlers::accessLog)
                .next(next -> Handlers.exceptionHandler(next)
                        .addExceptionHandler(Throwable.class, ExceptionHandlers::handleAllExceptions))
                .complete(handler);
    }
    
    public void shutdown() {
        if (listenerManager != null) listenerManager.shutdown();
        if (commandManager != null) {
            eventBus.unregister(commandManager);
            commandManager.shutdown();
        }
        if (mbd != null) mbd.shutdown();
        if (jda != null) jda.shutdown();
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            logger.error("Nie podano tokenu");
            System.exit(1);
        }

        if (args.length > 1 && args[1].equals("debug")) {
            logger.warn("= TRYB DEBUG WŁĄCZONY =");
            logger.warn("Stacktrace będzie wysyłany do każdego RestAction, ma to negatywny wpływ na wydajność!");
            RestAction.setPassContext(true);
            RestAction.setDefaultFailure(ContextException.herePrintingTrace());
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        new Main(args[0]);
    }
}
