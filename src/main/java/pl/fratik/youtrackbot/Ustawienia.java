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

public class Ustawienia {
    public static Ustawienia instance;
    public String botGuild = "ID serwera, na którym bot ma odpowiadać";
    public String youTrackUrl = "url youtracka bez /api";
    public String hubUrl = "url huba z /hub, jeżeli tak jest skonfigurowany";
    public String scope = "ID service'u youtracka";
    public String id = "patrz readme";
    public String secret = "patrz readme";
}
