/*
 *
 * Copyright 2022 SpotifyB0T
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package me.kamelajda.valorantstats.redis.services;

import lombok.extern.slf4j.Slf4j;
import me.kamelajda.valorantstats.redis.data.ValorantAccountState;
import me.kamelajda.valorantstats.redis.repositories.RedisValorantAccountStateRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class RedisValorantAccountStateService implements RedisService<ValorantAccountState, String> {

    private final RedisValorantAccountStateRepository redisRepository;

    public RedisValorantAccountStateService(RedisValorantAccountStateRepository redisRepository) {
        this.redisRepository = redisRepository;
    }

    @Override
    public ValorantAccountState save(ValorantAccountState value) {
        value.setTimeToLive(1440L); // 1 day
        value.setId(value.getName() + "#" + value.getTag());
        return redisRepository.save(value);
    }

    @Override
    public Optional<ValorantAccountState> findValueById(String id) {
        return redisRepository.findById(id);
    }

    public Optional<ValorantAccountState> findByNameAndTag(String name, String tag) {
        return redisRepository.findById(name + "#" + tag);
    }

}
