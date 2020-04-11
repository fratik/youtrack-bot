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

package pl.fratik.youtrackbot.api.models.impl;

import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import pl.fratik.youtrackbot.api.YouTrack;
import pl.fratik.youtrackbot.api.exceptions.APIException;
import pl.fratik.youtrackbot.api.models.Issue;
import pl.fratik.youtrackbot.api.models.Project;

import java.util.List;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class ProjectImpl implements Project {
    public static final String FIELDS = "name,id,shortName";

    private final String name;
    private final String id;
    private final String shortName;
    @Expose(serialize=false,deserialize=false)
    private YouTrack youTrack;

    @Override
    public List<Issue> retrieveIssues() throws APIException {
        if (youTrack == null) throw new APIException("Nie ustawiono instancji YouTrack'a");
        return youTrack.getIssues(this);
    }
}
