package me.kamelajda.valorantstats.utils.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum GameType {

    COMPETITIVE("competitive"),
    UNRATED("unrated"),
    DEATHMATCH("deathmatch"),
    SPIKERUSH("spikerush"),
    ESCELATION("escalation"),
    REPLICATION("replication"),
    NEWMAP("newmap"),
    SWIFTPLAY("swiftplay"),
    CUSTOM("custom");

    private final String text;

}
