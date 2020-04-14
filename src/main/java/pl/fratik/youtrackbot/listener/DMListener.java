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

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.fratik.youtrackbot.Ustawienia;
import pl.fratik.youtrackbot.api.YouTrack;
import pl.fratik.youtrackbot.api.auth.OAuth2RefreshToken;
import pl.fratik.youtrackbot.api.exceptions.APIException;
import pl.fratik.youtrackbot.api.exceptions.UnauthorizedException;
import pl.fratik.youtrackbot.api.models.Project;
import pl.fratik.youtrackbot.api.models.User;
import pl.fratik.youtrackbot.entity.UserKey;
import pl.fratik.youtrackbot.entity.UserKeyDao;

import java.util.List;

public class DMListener implements Listener {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final UserKeyDao ukd;
    private List<Project> projects;

    public DMListener(UserKeyDao ukd) {
        this.ukd = ukd;
    }

    @Subscribe
    @AllowConcurrentEvents
    public void onMessage(MessageReceivedEvent e) {
        if (e.isFromGuild() || e.getAuthor().isBot()) return;
        if (!e.getMessage().getContentRaw().matches("^[A-Za-z0-9]+$")) {
            e.getChannel().sendMessage("To nie wygląda na prawidłowy kod! " +
                    "Jeżeli piszesz do mnie bo chcesz się pobawić, to nie, nic tu nie znajdziesz. " +
                    "Jeżeli nie masz pojęcia o jaki kod chodzi: wygenerujesz go na <" + YouTrack.getOAuth2AuthUrl() +
                    ">. Jeżeli podałeś/aś poprawny kod, zgłoś to fratikowi.").complete();
            return;
        }
        OAuth2RefreshToken auth = new OAuth2RefreshToken(Ustawienia.instance.id, Ustawienia.instance.secret,
                null, Ustawienia.instance.redirect);
        String refreshToken;
        User usr;
        try {
            refreshToken = auth.getRefreshToken(e.getMessage().getContentRaw());
            auth.setRefreshToken(refreshToken);
            usr = new YouTrack(Ustawienia.instance.youTrackUrl, auth).getMe();
        } catch (UnauthorizedException unauthorizedException) {
            e.getChannel().sendMessage("Podano nieprawidłowy kod.").complete();
            return;
        } catch (APIException apiException) {
            e.getChannel().sendMessage("Wystąpił nieoczekiwany błąd.").complete();
            LoggerFactory.getLogger(getClass())
                    .error(String.format("Nieoczekiwany błąd dla kodu %s:", e.getMessage().getContentRaw()), apiException);
            return;
        }
        e.getChannel().sendMessage(String.format("Pomyślnie zalogowano jako %s (%s)!",
                usr.getFullName(), usr.getLogin())).queue();
        UserKey uk = ukd.get(e.getAuthor());
        uk.setRefreshToken(refreshToken);
        ukd.save(uk);
    }
}
