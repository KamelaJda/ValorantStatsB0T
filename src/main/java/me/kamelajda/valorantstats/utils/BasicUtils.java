package me.kamelajda.valorantstats.utils;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;

import java.util.List;

public class BasicUtils {

    public static String getEmojiOrText(String emojiName, String text, Guild emojiServer) {
        List<RichCustomEmoji> emojis = emojiServer.getEmojisByName(emojiName, true);
        return emojis.isEmpty() ? text : emojis.get(0).getAsMention();
    }

    public static String getEmojiOrText(String emojiName, Guild emojiServer) {
        List<RichCustomEmoji> emojis = emojiServer.getEmojisByName(emojiName, true);
        return emojis.isEmpty() ? emojiName : emojis.get(0).getAsMention();
    }

}
