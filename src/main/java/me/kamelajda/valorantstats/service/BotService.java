package me.kamelajda.valorantstats.service;

import com.google.common.eventbus.EventBus;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.kamelajda.valorantstats.utils.EventWaiter;
import me.kamelajda.valorantstats.utils.Static;
import me.kamelajda.valorantstats.utils.language.Language;
import me.kamelajda.valorantstats.utils.language.LanguageType;
import me.kamelajda.valorantstats.utils.listener.JDAHandler;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.messages.MessageRequest;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Objects;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class BotService {

    private final String token;
    private final LanguageType defaultLanguage;
    private final EventBus eventBus;
    private final EventWaiter eventWaiter;
    private final LanguageService languageService;
    private final Environment env;
    @Getter
    private ShardManager api;

    public BotService(Environment env, EventBus eventBus, EventWaiter eventWaiter, LanguageService languageService) {
        this.env = env;
        this.eventBus = eventBus;
        this.eventWaiter = eventWaiter;
        this.languageService = languageService;
        this.token = env.getProperty("discord.bot.token");
        this.defaultLanguage = LanguageType.valueOf(env.getProperty("application.defaultLanguage"));

        start();
    }

    public void start() {
        Language language = languageService.get(defaultLanguage);
        Static.defualtLanguage = language;

        log.info(language.get("status.translation.loaded"));

        JDAHandler eventHandler = new JDAHandler(eventBus);

        try {
            DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(token, GatewayIntent.GUILD_EMOJIS_AND_STICKERS, GatewayIntent.GUILD_MEMBERS);
            builder.addEventListeners(eventHandler, eventWaiter);
            builder.setShardsTotal(Objects.requireNonNull(env.getProperty("jda.shards.total", Integer.class)));
            builder.setShards(Objects.requireNonNull(env.getProperty("jda.shards.min", Integer.class)), Objects.requireNonNull(env.getProperty("jda.shards.max", Integer.class)));
            builder.setEnableShutdownHook(false);
            builder.setAutoReconnect(true);
            builder.setStatus(OnlineStatus.DO_NOT_DISTURB);
            builder.setActivity(Activity.playing(language.get("status.starting")));
            builder.setBulkDeleteSplittingEnabled(false);
            builder.setCallbackPool(Executors.newFixedThreadPool(30));
            builder.enableCache(CacheFlag.MEMBER_OVERRIDES);
            builder.disableCache(CacheFlag.VOICE_STATE, CacheFlag.SCHEDULED_EVENTS, CacheFlag.ONLINE_STATUS, CacheFlag.FORUM_TAGS, CacheFlag.ROLE_TAGS, CacheFlag.ACTIVITY);
            MessageRequest.setDefaultMentionRepliedUser(false);
            MessageRequest.setDefaultMentions(EnumSet.of(Message.MentionType.EMOJI, Message.MentionType.CHANNEL));
            this.api = builder.build();
        } catch (Exception e) {
            log.error("Failed to login!", e);
            System.exit(1);
        }

        api.setStatus(OnlineStatus.ONLINE);
        api.setActivity(Activity.playing(language.get("status.hi")));
    }

}
