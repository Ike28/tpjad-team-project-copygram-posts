package com.ubb.tpjad.copygram_posts.api.dto;

import java.time.LocalDateTime;

public record PhotoMetadataDto (
        Long id,
        String filename,
        String contentType,
        Long size,
        Integer width,
        Integer height,
        LocalDateTime uploadedAt
) {
}
