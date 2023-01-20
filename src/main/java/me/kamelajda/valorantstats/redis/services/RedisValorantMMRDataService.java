package me.kamelajda.valorantstats.redis.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.kamelajda.valorantstats.redis.data.ValorantMMRData;
import me.kamelajda.valorantstats.redis.repositories.RedisValorantMMRDataRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class RedisValorantMMRDataService implements RedisService<ValorantMMRData, UUID> {

    private final RedisValorantMMRDataRepository redisRepository;

    @Override
    public ValorantMMRData save(ValorantMMRData value) {
        value.setTimeToLive(15L);
        value.setDate(Instant.now().getEpochSecond());
        return redisRepository.save(value);
    }

    @Override
    public Optional<ValorantMMRData> findValueById(UUID id) {
        return redisRepository.findById(id);
    }

}
