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
import lombok.SneakyThrows;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.fratik.youtrackbot.Ustawienia;
import pl.fratik.youtrackbot.api.YouTrack;
import pl.fratik.youtrackbot.api.models.Issue;
import pl.fratik.youtrackbot.api.models.Project;
import pl.fratik.youtrackbot.api.models.impl.IssueImpl;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IssueListener implements Listener {
    private static final Pattern BUILD = Pattern.compile("#(\\d+)");
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final YouTrack youtrack;
    private List<Project> projects;

    public IssueListener(YouTrack youtrack) {
        this.youtrack = youtrack;
        try {
            projects = youtrack.getProjects();
            executor.scheduleWithFixedDelay(this::updateProjects, 15, 15, TimeUnit.MINUTES);
        } catch (IOException e) {
            throw new IllegalArgumentException("Nieprawidłowe dane logowania do youtrack'a!", e);
        }
    }

    @SneakyThrows
    private void updateProjects() {
        projects = youtrack.getProjects();
    }

    @Subscribe
    @AllowConcurrentEvents
    public void onMessage(MessageReceivedEvent e) {
        if (!e.isFromGuild() || !e.getGuild().getId().equals(Ustawienia.instance.botGuild) ||
                e.getAuthor().isBot()) return;
        Matcher issue = YouTrack.ISSUE_ID.matcher(e.getMessage().getContentRaw());
        List<MessageEmbed> embedsToSend = new ArrayList<>();
        while (issue.find()) {
            String project = issue.group(1);
            String id = issue.group(2);
            try {
                boolean matches = false;
                for (Project p : projects) {
                    if (p.getShortName().equals(project)) {
                        matches = true;
                        break;
                    }
                }
                if (!matches) break;
                String iId = project + "-" + id;
                List<Issue> issues = youtrack.getIssues(iId);
                Issue iss = null;
                for (Issue i : issues) if (i.getIdReadable().equals(iId)) iss = i;
                if (iss == null) break;
                embedsToSend.add(generateEmbed(iss));
            } catch (Exception ex) {
                logger.error("Wystąpił błąd!", ex);
            }
        }
        if (embedsToSend.size() == 0) return;
        if (embedsToSend.size() == 1) e.getChannel().sendMessage(embedsToSend.get(0)).queue();
        else {
            e.getChannel().sendMessage("W wiadomości wykryto zgłoszenia lub buildy:").queue();
            int wyslano = 0;
            for (MessageEmbed em : embedsToSend) {
                e.getChannel().sendMessage(em).queue();
                wyslano++;
                if (wyslano == 3) {
                    int liczba = (embedsToSend.size() - wyslano);
                    if (liczba != 0) e.getChannel().sendMessage("...i " + liczba + " więcej").queue();
                    break;
                }
            }
        }
    }

    private MessageEmbed generateEmbed(Issue i) {
        Issue.Field priorytet = null;
        Issue.Field typ = null;
        Issue.Field stan = null;
        Issue.Field przypisane = null;
        Issue.Field tester = null;
        Issue.Field wynikTestu = null;
        for (Issue.Field f : i.getFields()) {
            if (f.getName().equals("Priority")) priorytet = f;
            if (f.getName().equals("Type")) typ = f;
            if (f.getName().equals("State")) stan = f;
            if (f.getName().equals("Assignee")) przypisane = f;
            if (f.getName().equals("Tester")) tester = f;
            if (f.getName().equals("Wyniki testu")) wynikTestu = f;
        }
        IssueImpl.FieldImpl.FieldValueImpl nullField = new IssueImpl.FieldImpl.FieldValueImpl() {
            @Override
            public String getName() {
                return "Brak";
            }
        };
        List<IssueImpl.FieldImpl.FieldValueImpl> nullFields = new ArrayList<>();
        nullFields.add(nullField);
        if (priorytet.getValue() == null) ((IssueImpl.FieldImpl) priorytet).setValue(nullFields);
        if (typ.getValue() == null) ((IssueImpl.FieldImpl) typ).setValue(nullFields);
        if (stan.getValue() == null) ((IssueImpl.FieldImpl) stan).setValue(nullFields);
        if (przypisane.getValue() == null) ((IssueImpl.FieldImpl) przypisane).setValue(nullFields);
        if (tester.getValue() == null) ((IssueImpl.FieldImpl) tester).setValue(nullFields);
        if (wynikTestu.getValue() == null) ((IssueImpl.FieldImpl) wynikTestu).setValue(nullFields);
        return new EmbedBuilder().setAuthor(i.getIdReadable()).setTitle(i.getSummary())
                .setDescription(i.getDescription()).setColor(priorytet.getValue().get(0).getColor().getBackground())
                .addField("Priorytet", priorytet.getValue().get(0).getName(), true)
                .addField("Typ", typ.getValue().get(0).getName(), true)
                .addField("Stan", stan.getValue().get(0).getName(), true)
                .addField("Przypisane do", przypisane.getValue().get(0).getName(), true)
                .addField("Tester", tester.getValue().get(0).getName(), true)
                .addField("Wyniki testu", wynikTestu.getValue().get(0).getName(), true)
                .addField("Otwórz w przeglądarce", "[Klik](" + Ustawienia.instance.youTrackUrl +
                        "/issue/" + i.getIdReadable() + ")", false)
                .setFooter(i.getReporter().getFullName(), i.getReporter().getAvatarUrl())
                .setTimestamp(Instant.ofEpochMilli(i.getCreated())).build();
    }

    @Override
    public void shutdown() {
        executor.shutdown();
    }
}
