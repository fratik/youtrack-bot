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

package pl.fratik.youtrackbot.commands;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import pl.fratik.youtrackbot.Ustawienia;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandManager {
    private static final Pattern PREFIX_REGEX = Pattern.compile(Message.MentionType.USER.getPattern().toString() + ",? ");
    private final Map<String, Command> commands;
    private final EventBus eventBus;

    public CommandManager(EventBus eventBus) {
        this.eventBus = eventBus;
        commands = new HashMap<>();
    }

    @Subscribe
    @AllowConcurrentEvents
    public void onMessage(MessageReceivedEvent e) {
        if (!e.isFromGuild() || !e.getGuild().getId().equals(Ustawienia.instance.botGuild) || e.getAuthor().isBot())
            return;
        if (!e.getMessage().isMentioned(e.getJDA().getSelfUser(), Message.MentionType.USER)) return;
        Matcher matcher = PREFIX_REGEX.matcher(e.getMessage().getContentRaw());
        if (!matcher.find()) return;
        List<String> args = new ArrayList<>(Arrays.asList(matcher.replaceFirst("").split(" ")));
        if (args.size() == 0) return;
        String command = args.remove(0);
        Command c = commands.get(command);
        if (c == null) return;
        Context ctx = new Context(e, args);
        c.execute(ctx);
    }

    public synchronized void registerCommands(List<Command> commands) {
        for (Command command : commands) {
            this.commands.put(command.getName(), command);
        }
    }

    public synchronized void shutdown() {
        commands.clear();
    }
}
