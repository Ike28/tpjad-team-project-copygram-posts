package com.ubb.tpjad.copygram_posts.service;

import com.ubb.tpjad.copygram_posts.api.CopygramPostAPI;
import com.ubb.tpjad.copygram_posts.api.dto.PhotoMetadataDto;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.UncheckedIOException;

@Slf4j
@Service
public class RemotePhotoClient {
    private final RestClient restClient;

    @Value("${photos.path}")
    private String photosPath;

    public RemotePhotoClient(@Value("${photos.base-url}") String photosBaseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(photosBaseUrl)
                .build();
    }

    public ResponseEntity<byte[]> retrievePostPhoto(String photoId, String authHeader) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(photosPath)
                        .pathSegment("{%s}".formatted(photoId))
                        .build())
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    if (res.getStatusCode().value() == 404) {
                        log.warn("Photo with id {} not found", photoId);
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Photo not found");
                    }
                    if (res.getStatusCode().value() == 401) {
                        log.error("Photo service rejected unauthorized request for photo {}", photoId);
                        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
                    }

                    log.error("Photo service rejected bad request with status {}.", res.getStatusCode().value());
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Photo service rejected request");
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    log.error("Photo service retrieval request for photo {} failed exceptionally with status {}.", photoId, res.getStatusCode().value());
                    throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Photo service error");
                })
                .toEntity(byte[].class);
    }

    public ResponseEntity<PhotoMetadataDto> uploadPostPhoto(MultipartFile file, String description, String userId) {
        val body = new LinkedMultiValueMap<String, Object>();

        body.add(CopygramPostAPI.POST_PHOTO_FILE_REQUEST_PARAM, new ByteArrayResource(toBytes(file)) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        });
        body.add(CopygramPostAPI.POST_DESCRIPTION_REQUEST_PARAM, description);
        body.add(CopygramPostAPI.PHOTO_USER_ID_REQUEST_PARAM, userId);

        return restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(photosPath)
                        .build())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    if (res.getStatusCode().value() == 401) {
                        log.error("Photo service rejected unauthorized photo upload request from user {}", userId);
                        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
                    }
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Photo service rejected request");
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    log.error("Photo service upload request from user {} failed exceptionally with status {}.", userId, res.getStatusCode().value());
                    throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Photo service error");
                })
                .toEntity(PhotoMetadataDto.class);
    }

    private byte[] toBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
