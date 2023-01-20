package me.kamelajda.valorantstats.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class MatchDto {

    private final MatchMetadata metadata;

    private final MatchPlayers players;

    private final Map<String, MatchTeam> teams;

    private final List<KillData> kills;

    @Data
    @RequiredArgsConstructor
    public static class MatchMetadata {
        private final String map;

        @SerializedName("game_length")
        private final long gameLength;

        @SerializedName("game_start")
        private final long gameStart;

        private final String mode;

        @SerializedName("matchid")
        private final UUID matchId;
    }

    @Data
    @RequiredArgsConstructor
    public static class KillData {

        @SerializedName("killer_display_name")
        private final String killerDisplayName;

        @SerializedName("killer_puuid")
        private final UUID killerPUUID;

        @SerializedName("victim_display_name")
        private final String victimDisplayName;

        @SerializedName("victim_puuid")
        private final UUID victimPUUID;

        private final List<AssistantsData> assistants;

        @SerializedName("damage_weapon_name")
        private final String damageWeaponName;

        @Data
        @RequiredArgsConstructor
        public static class AssistantsData {
            @SerializedName("assistant_display_name")
            private final String assistantDisplayName;

            @SerializedName("assistant_puuid")
            private final UUID assistantPUUID;
        }

    }

    @RequiredArgsConstructor
    @Data
    public static class PlayerMatch {
        private UUID puuid;
        private String name;
        private String tag;
        private String team;
        private int level;
        private String character;

        @SerializedName("currenttier")
        private int currentTier;

        @SerializedName("session_playtime")
        private SessionPlaytime sessionPlaytime;

        @SerializedName("ability_casts")
        private AbilityCasts abilityCasts;

        private PlayerStats stats;

        @RequiredArgsConstructor
        @Data
        public static class SessionPlaytime {
            private final long minutes;
            private final long seconds;
            private final long miliseconds;
        }

        @RequiredArgsConstructor
        @Data
        public static class AbilityCasts {

            @SerializedName("c_cast")
            private int cCast;

            @SerializedName("q_cast")
            private int qCast;

            @SerializedName("e_cast")
            private int eCast;

            @SerializedName("x_cast")
            private int xCast;
        }

        @RequiredArgsConstructor
        @Data
        public static class PlayerStats {
            private long score;
            private long kills;
            private long deaths;
            private long assists;
            private long bodyshots;
            private long headshots;
            private long legshots;
        }
    }

    @RequiredArgsConstructor
    @Data
    public static class MatchPlayers {
        @SerializedName("all_players")
        private final List<PlayerMatch> allPlayers;
        private final List<PlayerMatch> red;
        private final List<PlayerMatch> blue;
    }

    @RequiredArgsConstructor
    @Data
    public static class MatchTeam {

        @SerializedName("has_won")
        private final boolean hasWon;

        @SerializedName("rounds_won")
        private final int roundsWon;

        @SerializedName("rounds_lost")
        private final int roundsLost;
    }

}
