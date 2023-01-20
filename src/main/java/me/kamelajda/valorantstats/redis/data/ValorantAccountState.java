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
@RedisHash("AccountState")
@ToString
public class ValorantAccountState extends RedisExpireEntity implements Serializable {

    /**
     * Generate object ID from {@link ValorantAccountState#getName()} + {@link ValorantAccountState#getTag()}
     */
    @Id
    private String id;

    /**
     * Account ID
     */
    private UUID puuid;

    /**
     * Region
     */
    private String region;

    /**
     * Account Level
     */
    @SerializedName("account_level")
    private int accountLevel;

    /**
     * Account name
     */
    private String name;

    /**
     * Account tag
     */
    private String tag;

    /**
     * Account card
     */
    private Card card;

    /**
     * Last fetch
     */
    @SerializedName("last_update_raw")
    private long lastUpdate;

    @Data
    @Getter
    @ToString
    public static class Card implements Serializable {
        private String small;
        private String large;
        private String wide;
        private String id;
    }

}
