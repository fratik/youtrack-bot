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

import net.dv8tion.jda.api.entities.User;
import pl.fratik.youtrackbot.Ustawienia;
import pl.fratik.youtrackbot.api.YouTrack;
import pl.fratik.youtrackbot.api.auth.OAuth2RefreshToken;
import pl.fratik.youtrackbot.api.exceptions.APIException;
import pl.fratik.youtrackbot.api.exceptions.UnauthorizedException;
import pl.fratik.youtrackbot.api.models.Issue;
import pl.fratik.youtrackbot.entity.UserKey;
import pl.fratik.youtrackbot.entity.UserKeyDao;

import java.util.List;

public class TesterCommand extends Command {
    private final UserKeyDao userKeyDao;

    public TesterCommand(UserKeyDao userKeyDao) {
        this.userKeyDao = userKeyDao;
        name = "tester";
    }

    @Override
    public void execute(Context ctx) {
        User sender = ctx.getSender();
        String issueId;
        if (ctx.getArgs().size() == 0) {
            ctx.send("Musisz podać ID zgłoszenia, do którego chcesz się przypisać.");
            return;
        } else issueId = ctx.getArgs().get(0);
        if (!YouTrack.ISSUE_ID.matcher(issueId).matches()) {
            ctx.send("To nie jest prawidłowe ID zgłoszenia.");
            return;
        }
        UserKey uk = userKeyDao.get(sender);
        if (uk.getRefreshToken() == null) {
            ctx.send("Nie jesteś zalogowany/a! Odwiedź <" + YouTrack.getOAuth2AuthUrl() + ">, " +
                    "zaloguj się i wyślij kod ze strony do mnie w DM.");
            return;
        }
        try {
            YouTrack yt = new YouTrack(Ustawienia.instance.youTrackUrl, new OAuth2RefreshToken(Ustawienia.instance.id,
                    Ustawienia.instance.secret, uk.getRefreshToken(), Ustawienia.instance.redirect));
            List<Issue> issues = yt.getIssues(issueId);
            if (issues.size() != 1) {
                ctx.send("Nie znaleziono takiego zgłoszenia.");
                return;
            }
            Issue.Field tester = null;
            for (Issue.Field f : issues.get(0).getFields()) if (f.getName().equals("Tester")) tester = f;
            if (tester == null) throw new APIException("brak pola tester", null);
            issues.get(0).setUserField(tester, yt.getMe());
            ctx.send("Pomyślnie przypisano cię do tego zgłoszenia!");
        } catch (UnauthorizedException e) {
            ctx.send("Wygląda na to, że zabrałeś/aś mi dostęp do Twojego konta. " +
                    "Odwiedź <" + YouTrack.getOAuth2AuthUrl() + ">, by zalogować się ponownie.");
        } catch (APIException e) {
            ctx.send("Wystąpił błąd z API!");
        }
    }
}
