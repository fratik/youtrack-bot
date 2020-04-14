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

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class Context {
    private final MessageReceivedEvent event;
    private final List<String> args;

    public Context(MessageReceivedEvent event, List<String> args) {
        this.event = event;
        this.args = args;
    }

    public Message getMessage() {
        return event.getMessage();
    }

    @Deprecated
    public MessageReceivedEvent getEvent() {
        return event;
    }

    public MessageChannel getChannel() {
        return event.getChannel();
    }

    public TextChannel getTextChannel() {
        return !event.isFromGuild() ? null : event.getTextChannel();
    }

    public List<String> getArgs() {
        return args;
    }

    public Message send(String cnt) {
        return getChannel().sendMessage(cnt).complete();
    }

    public User getSender() {
        return event.getAuthor();
    }
}
