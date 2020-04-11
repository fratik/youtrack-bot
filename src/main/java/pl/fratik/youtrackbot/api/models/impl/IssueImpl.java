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

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import pl.fratik.youtrackbot.api.models.Issue;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class IssueImpl implements Issue {
    public static final String FIELDS = "id,idReadable," +
            "customFields(name,value(name,description,color(background,foreground)),id),summary,description," +
            "project(name,id,shortName),reporter(login,fullName,avatarUrl),attachments(url,name),created";

    private List<AttachmentImpl> attachments;
    private long created;
    private String description;
    private ProjectImpl project;
    @SerializedName("customFields")
    private List<FieldImpl> fields;
    private String idReadable;
    private String summary;
    private String id;
    private UserImpl reporter;

    @Override
    public List<Attachment> getAttachments() {
        if (attachments == null) return null;
        return Collections.unmodifiableList(attachments.stream().map(a -> (Attachment) a).collect(Collectors.toList()));
    }

    @Override
    public List<Field> getFields() {
        if (fields == null) return null;
        return Collections.unmodifiableList(fields.stream().map(a -> (Field) a).collect(Collectors.toList()));
    }

    @Data
    @AllArgsConstructor
    @RequiredArgsConstructor
    public static class AttachmentImpl implements Attachment {
        private String url;
        private String name;
    }

    @Data
    @AllArgsConstructor
    @RequiredArgsConstructor
    public static class FieldImpl implements Field {
        private String name;
        private String id;
        private List<FieldValueImpl> value;

        @Override
        public List<FieldValue> getValue() {
            if (value == null) return null;
            return Collections.unmodifiableList(value.stream().map(a -> (FieldValue) a).collect(Collectors.toList()));
        }

        @Data
        @AllArgsConstructor
        @RequiredArgsConstructor
        public static class FieldValueImpl implements FieldValue {
            private String name;
            private String description;
            private FieldValueColorImpl color;

            @Data
            public static class FieldValueColorImpl implements FieldValueColor {
                @SerializedName("background")
                private String backgroundRaw;
                @SerializedName("foreground")
                private String foregroundRaw;
            }
        }
    }
}
