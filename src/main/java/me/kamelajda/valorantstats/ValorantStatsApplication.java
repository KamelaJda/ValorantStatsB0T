package me.kamelajda.valorantstats;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;
import me.kamelajda.valorantstats.service.BotService;
import me.kamelajda.valorantstats.utils.EventBusErrorHandler;
import me.kamelajda.valorantstats.utils.EventWaiter;
import net.dv8tion.jda.api.sharding.ShardManager;
import okhttp3.OkHttpClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.Executors;

@SpringBootApplication
public class ValorantStatsApplication {

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder().build();
    }

    @Bean
    public EventBus eventBus() {
        return new AsyncEventBus(Executors.newFixedThreadPool(16), EventBusErrorHandler.instance);
    }

    @Bean
    public EventWaiter eventWaiter() {
        return new EventWaiter();
    }

    @Bean
    public ShardManager shardManager(BotService botService) {
        return botService.getApi();
    }

    @Bean
    public Gson gson() {
        return new Gson();
    }

    public static void main(String[] args) {
        SpringApplication.run(ValorantStatsApplication.class, args);
    }

}
