package com.ubb.tpjad.copygram_posts.service;

import com.ubb.tpjad.copygram_posts.api.CopygramPostAPI;
import com.ubb.tpjad.copygram_posts.api.dto.PhotoMetadataDto;
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

@Service
public class RemotePhotoClient {
    private final RestClient restClient;

    @Value("${photos.base-url}")
    private String photosBaseUrl;

    @Value("${photos.path}")
    private String photosPath;

    public RemotePhotoClient() {
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
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Photo not found");
                    }
                    if (res.getStatusCode().value() == 401) {
                        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
                    }
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Photo service rejected request");
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
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
                    if (res.getStatusCode().value() == 404) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Photo not found");
                    }
                    if (res.getStatusCode().value() == 401) {
                        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
                    }
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Photo service rejected request");
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
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
