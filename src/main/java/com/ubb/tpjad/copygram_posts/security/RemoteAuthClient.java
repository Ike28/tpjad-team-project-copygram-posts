package com.ubb.tpjad.copygram_posts.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Service
public class RemoteAuthClient {
    private final RestClient restClient;

    @Value("${auth.base-url}")
    private String authBaseUrl;

    @Value("${auth.path}")
    private String authPath;

    public RemoteAuthClient() {
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
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return RemoteAuthResult.invalid();
            }
            throw e;
        }
    }
}
