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

package pl.fratik.youtrackbot.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import gg.amy.pgorm.annotations.GIndex;
import gg.amy.pgorm.annotations.PrimaryKey;
import gg.amy.pgorm.annotations.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.beans.Transient;

@Table("keys")
@GIndex({"id"})
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class UserKey implements DatabaseEntity {

    public UserKey(String id) {
        this.id = id;
    }

    @PrimaryKey
    private String id;
    private String refreshToken;

    @Transient
    @JsonIgnore
    @Override
    public String getTableName() {
        return "keys";
    }

}
