package com.ubb.tpjad.copygram_posts.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubb.tpjad.copygram_posts.api.dto.PhotoMetadataDto;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.UncheckedIOException;

import static com.ubb.tpjad.copygram_posts.testutil.TestDataBuilder.*;
import static com.ubb.tpjad.copygram_posts.testutil.TestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class RemotePhotoClientTest {

    private MockWebServer mockWebServer;
    private RemotePhotoClient photoClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();
        photoClient = new RemotePhotoClient(baseUrl);

        // Set the photos path using reflection
        ReflectionTestUtils.setField(photoClient, "photosPath", "/photos");

        // Configure ObjectMapper to handle Java 8 date/time types
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    // ==================== uploadPostPhoto() Tests ====================

    @Test
    void uploadPostPhoto_Success_ReturnsPhotoMetadata() throws Exception {
        // Given
        MultipartFile file = buildMultipartFile();
        PhotoMetadataDto expectedMetadata = buildPhotoMetadataDto(TEST_PHOTO_ID);
        String jsonResponse = objectMapper.writeValueAsString(expectedMetadata);

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(jsonResponse));

        // When
        ResponseEntity<PhotoMetadataDto> result = photoClient.uploadPostPhoto(
                file, TEST_DESCRIPTION, TEST_USER_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().id()).isEqualTo(TEST_PHOTO_ID);

        // Verify request
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/photos");
        assertThat(request.getHeader("Content-Type")).contains("multipart/form-data");
    }

    @Test
    void uploadPostPhoto_401Unauthorized_ThrowsResponseStatusException() {
        // Given
        MultipartFile file = buildMultipartFile();
        mockWebServer.enqueue(new MockResponse().setResponseCode(401));

        // When & Then
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> photoClient.uploadPostPhoto(file, TEST_DESCRIPTION, TEST_USER_ID)
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exception.getReason()).contains("Unauthorized");
    }

    @Test
    void uploadPostPhoto_400BadRequest_ThrowsResponseStatusException() {
        // Given
        MultipartFile file = buildMultipartFile();
        mockWebServer.enqueue(new MockResponse().setResponseCode(400));

        // When & Then
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> photoClient.uploadPostPhoto(file, TEST_DESCRIPTION, TEST_USER_ID)
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getReason()).contains("Photo service rejected request");
    }

    @Test
    void uploadPostPhoto_500ServerError_ThrowsResponseStatusException() {
        // Given
        MultipartFile file = buildMultipartFile();
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        // When & Then
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> photoClient.uploadPostPhoto(file, TEST_DESCRIPTION, TEST_USER_ID)
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(exception.getReason()).contains("Photo service error");
    }

    @Test
    void uploadPostPhoto_503ServiceUnavailable_ThrowsResponseStatusException() {
        // Given
        MultipartFile file = buildMultipartFile();
        mockWebServer.enqueue(new MockResponse().setResponseCode(503));

        // When & Then
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> photoClient.uploadPostPhoto(file, TEST_DESCRIPTION, TEST_USER_ID)
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(exception.getReason()).contains("Photo service error");
    }

    @Test
    void uploadPostPhoto_FileIOException_ThrowsRuntimeException() {
        // Given
        MultipartFile file = buildMultipartFileWithIOException();

        // When & Then
        assertThrows(
                RuntimeException.class,
                () -> photoClient.uploadPostPhoto(file, TEST_DESCRIPTION, TEST_USER_ID)
        );
    }

    @Test
    void uploadPostPhoto_ValidMultipartParts_CorrectlyEncodesRequest() throws Exception {
        String filename = "special-chars-file.jpg";
        String description = "Test description with unicode: café ☕";
        MultipartFile file = buildMultipartFile(filename);
        PhotoMetadataDto metadata = buildPhotoMetadataDto(TEST_PHOTO_ID);
        String jsonResponse = objectMapper.writeValueAsString(metadata);

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(jsonResponse));

        photoClient.uploadPostPhoto(file, description, TEST_USER_ID);

        RecordedRequest request = mockWebServer.takeRequest();
        String bodyContent = request.getBody().readUtf8();

        assertThat(bodyContent).contains("file");
        assertThat(bodyContent).contains("description");
        assertThat(bodyContent).contains("userId");
        assertThat(bodyContent).contains(filename);
    }

    @Test
    void retrievePostPhoto_ThrowsIllegalArgumentException_DueToBugInProductionCode() {
        byte[] expectedBytes = TEST_FILE_CONTENT;
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "image/jpeg")
                .setBody(new String(expectedBytes)));

        assertThrows(
                IllegalArgumentException.class,
                () -> photoClient.retrievePostPhoto(TEST_PICTURE_ID, TEST_AUTH_HEADER)
        );
    }
}
