package me.kamelajda.valorantstats.commands;

import me.kamelajda.valorantstats.utils.UserUtil;
import me.kamelajda.valorantstats.utils.commands.ICommand;
import me.kamelajda.valorantstats.utils.commands.SlashContext;
import net.dv8tion.jda.api.EmbedBuilder;

import java.time.Instant;

public class ReportBugCommand extends ICommand {

    private static final String ISSUE_LINK = "https://github.com/KamelaJda/SpotifyB0T/issues/new/choose";

    public ReportBugCommand() {
        name = "reportbug";
    }

    @Override
    protected void execute(SlashContext context) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setFooter("ValorantStats", context.getJDA().getSelfUser().getEffectiveAvatarUrl());
        eb.setTimestamp(Instant.now());
        eb.setDescription(context.getLanguage().get("reportbug.embed.description", ISSUE_LINK));
        eb.setColor(UserUtil.getColor(context.getUser()));

        context.reply(eb.build());
    }

}
