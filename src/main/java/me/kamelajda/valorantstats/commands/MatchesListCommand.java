package me.kamelajda.valorantstats.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import me.kamelajda.valorantstats.component.ValorantAPI;
import me.kamelajda.valorantstats.dto.MatchDto;
import me.kamelajda.valorantstats.redis.data.ValorantAccountState;
import me.kamelajda.valorantstats.utils.BasicUtils;
import me.kamelajda.valorantstats.utils.EventWaiter;
import me.kamelajda.valorantstats.utils.UserUtil;
import me.kamelajda.valorantstats.utils.commands.ICommand;
import me.kamelajda.valorantstats.utils.commands.SlashContext;
import me.kamelajda.valorantstats.utils.enums.GameType;
import me.kamelajda.valorantstats.utils.enums.Rank;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.springframework.core.env.Environment;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class MatchesListCommand extends ICommand {
    private final ValorantAPI valorantAPI;
    private final Environment env;
    private final EventWaiter eventWaiter;

    public MatchesListCommand(ValorantAPI valorantAPI, Environment env, EventWaiter eventWaiter) {
        this.valorantAPI = valorantAPI;
        this.env = env;
        this.eventWaiter = eventWaiter;
        name = "matcheslist";
        usage = "<name:string> <tag:string> <gametype:string>";
    }

    @Override
    protected void updateOptionData(OptionData optionData, String key, String subcommand) {
        if (!key.equals("gametype")) return;

        for (GameType value : GameType.values()) {
            optionData.addChoice(value.getText(), value.name());
        }
    }

    @Override
    protected void execute(SlashContext context) {
        context.defer(false);

        try {
            ValorantAccountState account = valorantAPI.getAccount(context.getOption("name").getAsString(), context.getOption("tag").getAsString());

            String gameType = context.getOption("gametype").getAsString();

            List<MatchDto> list = valorantAPI.getMatchHistory(account.getPuuid(), account.getRegion(), GameType.valueOf(gameType));

            StringBuilder sb = new StringBuilder();

            int index = 0;
            int agentLength = 0;

            int headHits = 0;
            int bodyHits = 0;
            int legsHits = 0;

            Guild emojiServer = Objects.requireNonNull(context.getShardManager().getGuildById(Objects.requireNonNull(env.getProperty("discord.emojiserver"))));

            StringSelectMenu.Builder selectBuilder = StringSelectMenu.create("show-match");
            selectBuilder.setPlaceholder(context.getTranslate("matcheslist.choosematch"));
            selectBuilder.setMaxValues(1).setMinValues(1);

            for (MatchDto dto : list) {
                MatchDto.PlayerMatch player = dto.getPlayers().getAllPlayers().stream().filter(f -> f.getPuuid().equals(account.getPuuid())).findFirst().orElseThrow();
                MatchDto.MatchTeam team = dto.getTeams().getOrDefault(player.getTeam().toLowerCase(), new MatchDto.MatchTeam(false, -1, -1));

                headHits += player.getStats().getHeadshots();
                bodyHits += player.getStats().getBodyshots();
                legsHits += player.getStats().getLegshots();

                if (++index < 11) {
                    String won = team.isHasWon() ? context.getTranslate("matcheslist.won") : context.getTranslate("matcheslist.lost");

                    List<RichCustomEmoji> emojis = emojiServer.getEmojisByName(player.getCharacter(), true);

                    String agentDisplay = BasicUtils.getEmojiOrText(player.getCharacter(), emojiServer);

                    String format = String.format("%s. %s - %s/%s/%s | %s - %s%n", index, agentDisplay, player.getStats().getKills(), player.getStats().getDeaths(), player.getStats().getAssists(), dto.getMetadata().getMap(), won);

                    sb.append(format);
                    if (player.getCharacter().length() > agentLength) agentLength = player.getCharacter().length();


                    String formatSubstring = (format.length() > SelectOption.LABEL_MAX_LENGTH ? format.substring(0, SelectOption.LABEL_MAX_LENGTH) : format).replace(agentDisplay + " -", "");
                    selectBuilder.addOption(formatSubstring, dto.getMetadata().getMatchId().toString(), null, emojis.isEmpty() ? null : emojis.get(0));
                }
            }

            int allHits = headHits + bodyHits + legsHits;

            String zsa = "\uDB40\uDC00\uDB40\uDC00 \uDB40\uDC00\uDB40\uDC00"
                + "\uDB40\uDC00\uDB40\uDC00 \uDB40\uDC00\uDB40\uDC00".repeat(agentLength * 2)
                + context.getTranslate("matcheslist.kda");

            EmbedBuilder eb = new EmbedBuilder();
            eb.setColor(UserUtil.getColor(context));
            eb.setDescription(zsa + "\n" + sb);
            eb.setTitle(context.getTranslate("matcheslist.history"));
            eb.setAuthor(account.getName() + "#" + account.getTag());
            eb.setThumbnail(account.getCard().getSmall());
            addAccuracy(eb, allHits, headHits, bodyHits, legsHits, context);
            addTopGuns(eb, list, account.getPuuid(), context);
            addTopMaps(eb, list, account.getPuuid(), context);
            addTopAgents(eb, list, account.getPuuid(), emojiServer, context);

            MessageCreateBuilder mcb = new MessageCreateBuilder();
            mcb.setEmbeds(eb.build());
            mcb.setComponents(ActionRow.of(selectBuilder.build()));

            Message mainMessage = context.send(mcb.build());

            eventWaiter.waitForEvent(StringSelectInteractionEvent.class,
                e -> e.getUser().getIdLong() == context.getUser().getIdLong() && e.getComponentId().equals("show-match"),
                (e) -> {
                    MatchDto match = list.stream()
                        .filter(f -> f.getMetadata().getMatchId().toString().equals(e.getValues().get(0)))
                        .findAny().orElse(null);

                    if (match == null) {
                        e.getHook().setEphemeral(true).sendMessage(context.getTranslate("matcheslist.notfound")).complete();
                        return;
                    }

                    EmbedBuilder matchEmbed = new EmbedBuilder();
                    matchEmbed.setColor(UserUtil.getColor(context));
                    matchEmbed.setTitle(match.getMetadata().getMap() + " | " + match.getTeams().get("red").getRoundsWon() + "-" + match.getTeams().get("blue").getRoundsWon());
                    addMatchInfo(matchEmbed, match, context);
                    addTeamsUsers(matchEmbed, match, account.getPuuid(), emojiServer, context);

                    e.replyEmbeds(matchEmbed.build()).queue();
                },
                10, TimeUnit.MINUTES, () -> {
                    mainMessage.editMessageComponents(ActionRow.of(selectBuilder.setDisabled(true).build())).complete();
                });

        } catch (Exception e) {
            log.error("Error", e);
            context.send("Wystąpił błąd");
       }
    }

    private void addMatchInfo(EmbedBuilder eb, MatchDto match, SlashContext context) {
        MatchDto.MatchMetadata metadata = match.getMetadata();

        StringBuilder sb = new StringBuilder();

        sb.append(context.getTranslate("matcheslist.natchinfo.mode", metadata.getMode())).append("\n");
        sb.append(context.getTranslate("matcheslist.natchinfo.start", String.format("<t:%s:f>", metadata.getGameStart()))).append("\n");

        long minutes = (metadata.getGameLength() / 1_000) / 60;
        long seconds = (metadata.getGameLength() / 1_000) % 60;

        sb.append(context.getTranslate("matcheslist.matchlenght", context.getTranslate("matcheslist.timestamp", minutes, seconds))).append("\n");

        eb.addField("Match info", sb.toString(), false);
    }

    private void addTeamsUsers(EmbedBuilder eb, MatchDto match, UUID puuid, Guild emojiGuild, SlashContext context) {
        Comparator<Object> comparingLong = Comparator.comparingLong(x -> ((MatchDto.PlayerMatch) x).getStats().getAssists()).reversed();

        Function<List<MatchDto.PlayerMatch>, String> f = (playerMatches -> {
            StringBuilder stringBuilder = new StringBuilder();
            for (MatchDto.PlayerMatch playerMatch : playerMatches) {
                final String character = BasicUtils.getEmojiOrText(playerMatch.getCharacter(), emojiGuild);

                String s = String.format("%s/%s/%s", playerMatch.getStats().getKills(), playerMatch.getStats().getDeaths(), playerMatch.getStats().getAssists());

                if (playerMatch.getPuuid().equals(puuid)) stringBuilder.append("**");

                stringBuilder.append(character).append(" ");

                stringBuilder.append(playerMatch.getName());
                stringBuilder.append("`#").append(playerMatch.getTag()).append("` ");

                Rank rank = Rank.getFromTier(playerMatch.getCurrentTier());

                stringBuilder.append(BasicUtils.getEmojiOrText(rank.getEmojiName(), rank.getTranslateKey(), emojiGuild));

                stringBuilder.append(" ").append(s);

                if (playerMatch.getPuuid().equals(puuid)) stringBuilder.append("**");
                stringBuilder.append("\n");
            }

            return stringBuilder.toString();
        });

        List<MatchDto.PlayerMatch> red = match.getPlayers().getRed().stream().sorted(comparingLong).toList();
        List<MatchDto.PlayerMatch> blue = match.getPlayers().getBlue().stream().sorted(comparingLong).toList();

        String translate = context.getTranslate("matcheslist.wins");
        eb.addField("Red  " + (match.getTeams().get("red").isHasWon() ? translate : ""), f.apply(red), false);
        eb.addField("Blue  " + (match.getTeams().get("blue").isHasWon() ? translate : ""), f.apply(blue), false);
    }

    private void addAccuracy(EmbedBuilder eb, int allHits, int headHits, int bodyHits, int legsHits, SlashContext context) {
        StringBuilder accuracy = new StringBuilder();

        DecimalFormat format = new DecimalFormat("#.##");

        String headPrecent = format.format(((double) headHits * 100) / allHits);
        String bodyPrecent = format.format(((double) bodyHits * 100) / allHits);
        String legsPrecent = format.format(((double) legsHits * 100) / allHits);

        accuracy.append(context.getTranslate("matcheslist.accuracy.head", headPrecent, headHits)).append("\n");
        accuracy.append(context.getTranslate("matcheslist.accuracy.body", bodyPrecent, bodyHits)).append("\n");
        accuracy.append(context.getTranslate("matcheslist.accuracy.legs", legsPrecent, legsHits)).append("\n");

        eb.addField(context.getTranslate("matcheslist.accuracy.embed"), accuracy.toString(), true);
    }

    private void addTopGuns(EmbedBuilder eb, List<MatchDto> matches, UUID puuid, SlashContext context) {
        Map<String, Integer> map = new HashMap<>();

        for (MatchDto dto : matches) {
            List<MatchDto.KillData> playerKills = dto.getKills().stream().filter(f -> f.getKillerPUUID().equals(puuid)).toList();

            for (MatchDto.KillData kill : playerKills) {
                Integer integer = map.getOrDefault(kill.getDamageWeaponName(), 0);
                map.put(kill.getDamageWeaponName(), ++integer);
            }
        }

        StringBuilder sb = new StringBuilder();

        int index = 0;
        for (Map.Entry<String, Integer> entry : sortMap(map).entrySet()) {
            sb.append(context.getTranslate("matcheslist.kills", ++index, entry.getKey(), entry.getValue()));
        }

        eb.addField(context.getTranslate("matcheslist.embed.topguns"), sb.toString(), false);
    }

    private void addTopMaps(EmbedBuilder eb, List<MatchDto> matches, UUID puuid, SlashContext context) {
        Map<String, Integer> wins = new HashMap<>();

        for (MatchDto match : matches) {
            MatchDto.PlayerMatch player = match.getPlayers().getAllPlayers().stream()
                .filter(f -> f.getPuuid().equals(puuid))
                .findFirst().orElseThrow();

            MatchDto.MatchTeam team = match.getTeams().get(player.getTeam().toLowerCase());

            if (!team.isHasWon()) continue;

            Integer winCount = wins.getOrDefault(match.getMetadata().getMap(), 0);
            wins.put(match.getMetadata().getMap(), ++winCount);
        }

        StringBuilder sb = new StringBuilder();

        int index = 0;
        for (Map.Entry<String, Integer> entry : sortMap(wins).entrySet()) {
            sb.append(context.getTranslate("matcheslist.maps.wins", ++index, entry.getKey(), entry.getValue()));
        }

        eb.addField(context.getTranslate("matcheslist.embed.topmaps"), sb.toString(), false);
    }

    private void addTopAgents(EmbedBuilder eb, List<MatchDto> matches, UUID puuid, Guild emojiGuild, SlashContext context) {
        Map<String, Integer> allMatch = new HashMap<>();
        Map<String, AgentInfo> wins = new HashMap<>();
        Map<String, Integer> loses = new HashMap<>();

        for (MatchDto match : matches) {
            MatchDto.PlayerMatch player = match.getPlayers().getAllPlayers().stream()
                .filter(f -> f.getPuuid().equals(puuid))
                .findFirst().orElseThrow();

            MatchDto.MatchTeam team = match.getTeams().get(player.getTeam().toLowerCase());

            Integer integer = allMatch.getOrDefault(player.getCharacter(), 0);
            allMatch.put(player.getCharacter(), ++integer);

            if (team.isHasWon()) {
                AgentInfo winCount = wins.getOrDefault(player.getCharacter(), new AgentInfo(0, 0d));
                winCount.setMatchCount(winCount.getMatchCount() + 1);
                wins.put(player.getCharacter(), winCount);
            } else {
                Integer loseCount = loses.getOrDefault(player.getCharacter(), 0);
                loses.put(player.getCharacter(), ++loseCount);
            }
        }

        for (Map.Entry<String, AgentInfo> entry : wins.entrySet()) {
            AgentInfo value = entry.getValue();

            Integer allMatches = allMatch.getOrDefault(entry.getKey(), 0);
            if (allMatches == 0) continue;

            value.setPrecent(((double) value.getMatchCount() * 100) / allMatches);
        }

        Comparator<Object> comparingDouble = Comparator.comparingDouble(c -> {
            Map.Entry<String, AgentInfo> xd = (Map.Entry<String, AgentInfo>) c;
            return xd.getValue().getPrecent();
        }).reversed();

        Map<String, AgentInfo> sortedMap = wins.entrySet().stream()
            .sorted(comparingDouble)
            .limit(3)
            .filter(f -> !Objects.equals(f.getKey(), "null"))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        StringBuilder sb = new StringBuilder();

        int index = 0;
        DecimalFormat format = new DecimalFormat("#.##");
        for (Map.Entry<String, AgentInfo> entry : sortedMap.entrySet()) {
            final String agent = BasicUtils.getEmojiOrText(entry.getKey(), entry.getKey(), emojiGuild);

            sb.append(context.getTranslate("matcheslist.topagents", ++index, agent, format.format(entry.getValue().getPrecent()), entry.getValue().getMatchCount(), loses.getOrDefault(entry.getKey(), 0)));
        }

        eb.addField(context.getTranslate("matcheslist.embed.topagents"), sb.toString(), false);
    }

    private Map<String, Integer> sortMap(Map<String, Integer> map) {
        return map.entrySet().stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .limit(3)
            .filter(f -> !Objects.equals(f.getKey(), "null"))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }

    @Data
    @AllArgsConstructor
    private static class AgentInfo {
        private int matchCount;
        private Double precent;
    }

}
