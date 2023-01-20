package me.kamelajda.valorantstats.service;

import com.google.common.eventbus.EventBus;
import jdk.jfr.Event;
import lombok.extern.slf4j.Slf4j;
import me.kamelajda.valorantstats.commands.MatchesListCommand;
import me.kamelajda.valorantstats.commands.ProfileCommand;
import me.kamelajda.valorantstats.commands.ReportBugCommand;
import me.kamelajda.valorantstats.component.ValorantAPI;
import me.kamelajda.valorantstats.utils.EventWaiter;
import me.kamelajda.valorantstats.utils.commands.CommandExecute;
import me.kamelajda.valorantstats.utils.commands.CommandManager;
import me.kamelajda.valorantstats.utils.language.LanguageType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
import net.dv8tion.jda.api.interactions.commands.localization.ResourceBundleLocalizationFunction;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class CommandService {

    private final CommandManager commandManager;
    private final CommandExecute commandExecute;
    private final EventBus eventBus;
    private final ShardManager api;
    private final ValorantAPI valorantAPI;
    private final Environment env;
    private final EventWaiter eventWaiter;

    public CommandService(LanguageService languageService, EventBus eventBus, ShardManager api, ValorantAPI valorantAPI, Environment env, EventWaiter eventWaiter) {
        this.api = api;
        this.env = env;
        this.eventBus = eventBus;
        this.valorantAPI = valorantAPI;
        this.eventWaiter = eventWaiter;
        this.commandManager = new CommandManager();
        this.commandExecute = new CommandExecute(commandManager, languageService, eventBus);

        initCommands();
        init();
    }

    private void initCommands() {
        commandManager.registerCommand(new ReportBugCommand());
        commandManager.registerCommand(new ProfileCommand(valorantAPI, env));
        commandManager.registerCommand(new MatchesListCommand(valorantAPI, env, eventWaiter));
    }

    private void init() {
        LocalizationFunction localizationFunction = ResourceBundleLocalizationFunction
            .fromBundles("language/messages", Arrays.stream(LanguageType.values()).map(LanguageType::getDiscordLocale).toArray(DiscordLocale[]::new))
            .build();

        List<CommandDataImpl> data = commandManager.getCommands().values().stream()
            .map(cmd -> cmd.createCommandDate(localizationFunction)).toList();

        for (JDA shard : api.getShards()) {
            shard.updateCommands().addCommands(data).queue();
        }

        eventBus.register(commandExecute);
    }

}
