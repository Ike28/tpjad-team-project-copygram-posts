package com.ubb.tpjad.copygram_posts.mapper;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
class IdMapper {
    String mapId(String entityId) {
        return Optional.ofNullable(entityId).orElse(UUID.randomUUID().toString());
    }
}
