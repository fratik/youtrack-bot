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

package pl.fratik.youtrackbot.api.models.deserializer;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import pl.fratik.youtrackbot.api.models.Issue;
import pl.fratik.youtrackbot.api.models.impl.IssueImpl;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FieldDeserializer implements JsonDeserializer<Issue.Field> {
    @Override
    public Issue.Field deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String name;
        String id;
        List<IssueImpl.FieldImpl.FieldValueImpl> values = new ArrayList<>();
        JsonObject obj = json.getAsJsonObject();
        name = obj.get("name").getAsString();
        id = obj.get("id").getAsString();
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        JsonElement vals = obj.get("value");
        if (vals.isJsonArray()) {
            values = gson.fromJson(vals, new TypeToken<List<IssueImpl.FieldImpl.FieldValueImpl>>(){}.getType());
        } else if (vals.isJsonObject()) {
            IssueImpl.FieldImpl.FieldValueImpl tak;
            tak = gson.fromJson(vals, new TypeToken<IssueImpl.FieldImpl.FieldValueImpl>() {}.getType());
            if (tak == null) values = null;
            else values.add(tak);
        } else {
            IssueImpl.FieldImpl.FieldValueImpl tak;
            if (vals.isJsonNull()) values = null;
            else {
                tak = new IssueImpl.FieldImpl.FieldValueImpl() {
                    @Override
                    public String getName() {
                        return vals.getAsJsonPrimitive().toString();
                    }
                };
                values.add(tak);
            }
        }
        return new IssueImpl.FieldImpl(name, id, values);
    }
}
