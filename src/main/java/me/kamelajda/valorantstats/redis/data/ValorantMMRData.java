/*
 *
 * Copyright 2022 SpotifyB0T
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package me.kamelajda.valorantstats.redis.data;

import com.google.gson.annotations.SerializedName;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Getter
@AllArgsConstructor
@Builder
@RedisHash("ValorantMMRData")
@ToString
public class ValorantMMRData extends RedisExpireEntity implements Serializable {

    /**
     * Account ID
     */
    @Id
    private UUID puuid;

    @SerializedName("current_data")
    private CurrentData currentData;

    @SerializedName("highest_rank")
    private HighestRank highestRank;

    private long date;

    @Getter
    @RequiredArgsConstructor
    public static class CurrentData implements Serializable {
        @SerializedName("currenttier")
        private final int currentTier;

        private final Images images;

        private final long elo;

        @Getter
        @RequiredArgsConstructor
        public static class Images {
            private final String small;
            private final String large;
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static class HighestRank implements Serializable {
        private final int tier;
        private final String season;
    }

}
