package cl.duoc.review.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import cl.duoc.review.dto.ApiResponse;
import cl.duoc.review.dto.ReviewCreateRequestDTO;
import cl.duoc.review.dto.ReviewResponseDTO;
import cl.duoc.review.dto.ReviewUpdateRequestDTO;
import cl.duoc.review.service.ReviewService;

@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

    @Mock
    private ReviewService reviewService;

    private ReviewController reviewController;

    private UUID destinationId;
    private UUID authorUserId;
    private ReviewResponseDTO reviewResponse;

    @BeforeEach
    void setUp() {
        reviewController = new ReviewController(reviewService);
        destinationId = UUID.randomUUID();
        authorUserId = UUID.randomUUID();
        reviewResponse = new ReviewResponseDTO(
                1L,
                destinationId,
                authorUserId,
                5,
                "Excelente destino",
                true,
                LocalDateTime.now(),
                null);
    }

    @Test
    void createReviewReturnsCreatedResponse() {
        String token = "Bearer token";
        ReviewCreateRequestDTO request = new ReviewCreateRequestDTO(destinationId, 5, "Excelente destino");
        when(reviewService.createReview(request, token)).thenReturn(reviewResponse);

        ResponseEntity<ApiResponse<?>> response = reviewController.createReview(token, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(201, response.getBody().getCode());
        assertEquals("Reseña creada correctamente", response.getBody().getMessage());
        assertEquals(reviewResponse, response.getBody().getData());
        verify(reviewService).createReview(request, token);
    }

    @Test
    void getAllReviewsReturnsOkResponse() {
        String token = "Bearer token";
        when(reviewService.getAllReviews(token)).thenReturn(List.of(reviewResponse));

        ResponseEntity<ApiResponse<?>> response = reviewController.getAllReviews(token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());
        assertEquals(List.of(reviewResponse), response.getBody().getData());
        verify(reviewService).getAllReviews(token);
    }

    @Test
    void getReviewByIdReturnsOkResponse() {
        String token = "Bearer token";
        when(reviewService.getReviewById(1L, token)).thenReturn(reviewResponse);

        ResponseEntity<ApiResponse<?>> response = reviewController.getReviewById(1L, token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());
        assertEquals(reviewResponse, response.getBody().getData());
        verify(reviewService).getReviewById(1L, token);
    }

    @Test
    void getReviewsByDestinationReturnsOkResponse() {
        String token = "Bearer token";
        when(reviewService.getReviewsByDestination(destinationId, token)).thenReturn(List.of(reviewResponse));

        ResponseEntity<ApiResponse<?>> response = reviewController.getReviewsByDestination(destinationId, token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());
        assertEquals(List.of(reviewResponse), response.getBody().getData());
        verify(reviewService).getReviewsByDestination(destinationId, token);
    }

    @Test
    void getReviewsByAuthorReturnsOkResponse() {
        String token = "Bearer token";
        when(reviewService.getReviewsByAuthor(authorUserId, token)).thenReturn(List.of(reviewResponse));

        ResponseEntity<ApiResponse<?>> response = reviewController.getReviewsByAuthor(authorUserId, token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());
        assertEquals(List.of(reviewResponse), response.getBody().getData());
        verify(reviewService).getReviewsByAuthor(authorUserId, token);
    }

    @Test
    void getAverageRatingByDestinationReturnsOkResponse() {
        String token = "Bearer token";
        when(reviewService.getAverageRatingByDestination(destinationId, token)).thenReturn(4.5);

        ResponseEntity<ApiResponse<?>> response = reviewController.getAverageRatingByDestination(destinationId, token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());
        assertEquals(4.5, response.getBody().getData());
        verify(reviewService).getAverageRatingByDestination(destinationId, token);
    }

    @Test
    void updateReviewReturnsOkResponse() {
        String token = "Bearer token";
        ReviewUpdateRequestDTO request = new ReviewUpdateRequestDTO(4, "Comentario actualizado");
        ReviewResponseDTO updatedResponse = new ReviewResponseDTO(
                1L,
                destinationId,
                authorUserId,
                4,
                "Comentario actualizado",
                true,
                LocalDateTime.now(),
                LocalDateTime.now());
        when(reviewService.updateReview(1L, request, token)).thenReturn(updatedResponse);

        ResponseEntity<ApiResponse<?>> response = reviewController.updateReview(1L, token, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());
        assertEquals(updatedResponse, response.getBody().getData());
        verify(reviewService).updateReview(1L, request, token);
    }

    @Test
    void deleteReviewReturnsOkResponse() {
        String token = "Bearer token";

        ResponseEntity<ApiResponse<?>> response = reviewController.deleteReview(1L, token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());
        assertEquals("Reseña eliminada correctamente", response.getBody().getMessage());
        verify(reviewService).deleteReview(1L, token);
    }
}
