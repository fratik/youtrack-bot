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

import com.google.common.eventbus.EventBus;
import lombok.Setter;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.VoiceDispatchInterceptor;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.handle.PresenceUpdateHandler;
import net.dv8tion.jda.internal.handle.SocketHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Map;

public class JDAEventHandler implements EventListener {
    private final EventBus eventBus;

    JDAEventHandler(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        eventBus.post(event);
    }
}
