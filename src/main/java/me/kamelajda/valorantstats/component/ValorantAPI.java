package me.kamelajda.valorantstats.component;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.kamelajda.valorantstats.dto.MatchDto;
import me.kamelajda.valorantstats.redis.data.ValorantAccountState;
import me.kamelajda.valorantstats.redis.data.ValorantMMRData;
import me.kamelajda.valorantstats.redis.services.RedisValorantAccountStateService;
import me.kamelajda.valorantstats.redis.services.RedisValorantMMRDataService;
import me.kamelajda.valorantstats.utils.enums.GameType;
import me.kamelajda.valorantstats.utils.exception.ResponseCodeException;
import me.kamelajda.valorantstats.utils.exception.ResponseNoDataFound;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValorantAPI {
    private static final String API_URL = "https://api.henrikdev.xyz/valorant/%s/%s";

    private final RedisValorantAccountStateService valorantAccountService;
    private final RedisValorantMMRDataService valorantMMrDataService;
    private final OkHttpClient okHttpClient;
    private final Gson gson;

    public ValorantAccountState getAccount(String name, String tag) throws Exception {
        Optional<ValorantAccountState> value = valorantAccountService.findByNameAndTag(name, tag);
        if (value.isPresent()) return value.get();

        HttpResponse request = request(String.format("account/%s/%s", name, tag), "v1");
        ValorantAccountState data = parseResponse(request.getJson().getAsJsonObject("data"), new TypeToken<ValorantAccountState>(){}.getType());
        valorantAccountService.save(data);
        return data;
    }

    public ValorantMMRData getMMRData(UUID puuid, String region) throws Exception {
        Optional<ValorantMMRData> value = valorantMMrDataService.findValueById(puuid);
        if (value.isPresent()) return value.get();

        HttpResponse request = request(String.format("by-puuid/mmr/%s/%s", region, puuid.toString()), "v2");

        ValorantMMRData data = parseResponse(request.getJson().getAsJsonObject("data"), new TypeToken<ValorantMMRData>(){}.getType());
        data.setPuuid(puuid);
        valorantMMrDataService.save(data);
        return data;
    }

    public List<MatchDto> getMatchHistory(UUID puuid, String region, GameType gameType) throws Exception {
        HttpResponse request = request(String.format("by-puuid/matches/%s/%s?filter=%s", region, puuid.toString(), gameType.getText()), "v3");

        return parseResponse(request.getJson().getAsJsonArray("data"), new TypeToken<List<MatchDto>>(){}.getType());
    }

    private <T> T parseResponse(JsonElement object, Type type) {
        return gson.fromJson(object, type);
    }

    @NotNull
    private HttpResponse request(String endpoint, String version) throws Exception {
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder();

        builder.url(String.format(API_URL, version, endpoint));

        try (Response call = okHttpClient.newCall(builder.build()).execute()) {
            HttpResponse request = new HttpResponse(gson.fromJson(call.body().string(), JsonObject.class), call.code());

            if (request.getCode() != 200) throw new ResponseCodeException(request.getCode());
            if (!request.getJson().has("data")) throw new ResponseNoDataFound();

            return request;
        } catch (Exception e) {
            log.error("Error", e);
            throw e;
        }
    }

    @RequiredArgsConstructor
    @Getter
    private static class HttpResponse {
        private final JsonObject json;
        private final int code;
    }

}
