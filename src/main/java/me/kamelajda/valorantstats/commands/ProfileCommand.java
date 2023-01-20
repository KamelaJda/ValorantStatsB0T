package me.kamelajda.valorantstats.commands;

import lombok.extern.slf4j.Slf4j;
import me.kamelajda.valorantstats.component.ValorantAPI;
import me.kamelajda.valorantstats.redis.data.ValorantAccountState;
import me.kamelajda.valorantstats.redis.data.ValorantMMRData;
import me.kamelajda.valorantstats.utils.BasicUtils;
import me.kamelajda.valorantstats.utils.UserUtil;
import me.kamelajda.valorantstats.utils.commands.ICommand;
import me.kamelajda.valorantstats.utils.commands.SlashContext;
import me.kamelajda.valorantstats.utils.enums.Rank;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.core.env.Environment;

import java.util.Objects;

@Slf4j
public class ProfileCommand extends ICommand {

    private final ValorantAPI valorantAPI;
    private final Environment env;

    public ProfileCommand(ValorantAPI valorantAPI, Environment env) {
        this.valorantAPI = valorantAPI;
        this.env = env;
        name = "profile";
        usage = "<name:string> <tag:string>";
    }

    @Override
    protected void execute(SlashContext context) {
        context.defer(false);

        Guild emojiServer = Objects.requireNonNull(context.getShardManager().getGuildById(Objects.requireNonNull(env.getProperty("discord.emojiserver"))));

        String name = context.getOption("name").getAsString();
        String tag = context.getOption("tag").getAsString();

        try {
            ValorantAccountState account = valorantAPI.getAccount(name, tag);
            ValorantMMRData mmr = valorantAPI.getMMRData(account.getPuuid(), account.getRegion());

            Rank currentRank = Rank.getFromTier(mmr.getCurrentData().getCurrentTier());
            String rank = context.getTranslate("profile.rank", String.format("%s %s", context.getTranslate(currentRank.getTranslateKey()), BasicUtils.getEmojiOrText(currentRank.getEmojiName(), "", emojiServer)));

            Rank fromTier = Rank.getFromTier(mmr.getHighestRank().getTier());
            String rankFormat = String.format("%s %s", context.getTranslate(fromTier.getTranslateKey()), BasicUtils.getEmojiOrText(fromTier.getEmojiName(), "", emojiServer));
            String highestRank = context.getTranslate("profile.rank", rankFormat);
            String season = context.getTranslate("profile.season", mmr.getHighestRank().getSeason());

            EmbedBuilder eb = new EmbedBuilder();
            eb.setColor(UserUtil.getColor(context));

            eb.setThumbnail(mmr.getCurrentData().getImages().getLarge());
            eb.setImage(account.getCard().getWide());
            eb.setAuthor(account.getName() + "#" + account.getTag(), null, null);

            eb.addField(context.getTranslate("profile.embed.accountlevle"), account.getAccountLevel() + "", false);
            eb.addField(context.getTranslate("profile.embed.region"), account.getRegion(), false);

            eb.addField(context.getTranslate("Current season"), context.getTranslate("profile.elo", mmr.getCurrentData().getElo()) + "\n" + rank, false);
            eb.addField(context.getTranslate("profile.embed.highestrank"), highestRank + "\n" + season, false);

            String translate = context.getTranslate("profile.embed.refresh.value",
                context.getTranslate("profile.embed.refresh.account"),
                String.format("<t:%s:f>", account.getLastUpdate()),
                context.getTranslate("profile.embed.refresh.rank"),
                String.format("<t:%s:f>", mmr.getDate()));

            eb.addField(context.getTranslate("profile.embed.refresh"), translate, false);

            context.send(eb.build());
        } catch (Exception e) {
            log.error("Error", e);
            context.send("Error!");
        }

    }

}
