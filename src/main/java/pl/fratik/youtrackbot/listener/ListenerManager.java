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

package pl.fratik.youtrackbot.listener;

import com.google.common.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class ListenerManager {
    private final List<Listener> listeners;
    private final EventBus eventBus;

    public ListenerManager(EventBus eventBus) {
        this.eventBus = eventBus;
        listeners = new ArrayList<>();
    }

    public synchronized void registerListeners(List<Listener> listeners) {
        for (Listener listener : listeners) {
            eventBus.register(listener);
            this.listeners.add(listener);
        }
    }

    public synchronized void shutdown() {
        for (Listener listener : listeners) {
            eventBus.unregister(listener);
            listener.shutdown();
        }
        listeners.clear();
    }
}
