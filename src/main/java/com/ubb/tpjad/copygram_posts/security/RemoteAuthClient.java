package com.ubb.tpjad.copygram_posts.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class RemoteAuthClient {
    private final RestClient restClient;

    @Value("${auth.path}")
    private String authPath;

    public RemoteAuthClient(@Value("${auth.base-url}") String authBaseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(authBaseUrl)
                .build();
    }

    public RemoteAuthResult validate(String jwtHeader) {
        try {
            return restClient.get()
                    .uri(authPath)
                    .header(HttpHeaders.AUTHORIZATION, jwtHeader)
                    .retrieve()
                    .body(RemoteAuthResult.class);
        } catch (HttpClientErrorException e) {
            log.error("Validation of request authorization with external service failed.", e);
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return RemoteAuthResult.invalid();
            }
            throw e;
        }
    }
}
