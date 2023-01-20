package me.kamelajda.valorantstats.redis.repositories;

import me.kamelajda.valorantstats.redis.data.ValorantMMRData;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.RepositoryDefinition;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RedisValorantMMRDataRepository extends CrudRepository<ValorantMMRData, UUID> {
}
