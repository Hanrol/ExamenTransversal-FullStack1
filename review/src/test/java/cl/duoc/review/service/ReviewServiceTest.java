package cl.duoc.review.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import cl.duoc.review.client.DestinationClient;
import cl.duoc.review.client.LoginClient;
import cl.duoc.review.dto.ReviewCreateRequestDTO;
import cl.duoc.review.dto.ReviewResponseDTO;
import cl.duoc.review.dto.ReviewUpdateRequestDTO;
import cl.duoc.review.dto.UserDTO;
import cl.duoc.review.model.Review;
import cl.duoc.review.repository.ReviewRepository;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private DestinationClient destinationClient;

    @Mock
    private LoginClient loginClient;

    private ReviewService reviewService;

    private UUID destinationId;
    private UUID authorUserId;
    private String token;
    private Review review;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewService(reviewRepository, destinationClient, loginClient);
        destinationId = UUID.randomUUID();
        authorUserId = UUID.randomUUID();
        token = "Bearer token";

        review = new Review();
        review.setReviewId(1L);
        review.setDestinationId(destinationId);
        review.setAuthorUserId(authorUserId);
        review.setRating(5);
        review.setComment("Excelente destino");
        review.setActive(true);
        review.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createReviewSavesReviewWhenTokenAndDestinationAreValid() {
        ReviewCreateRequestDTO request = new ReviewCreateRequestDTO(destinationId, 5, "Excelente destino");
        when(loginClient.validateToken("token")).thenReturn(new UserDTO(authorUserId, "victor"));
        when(destinationClient.destinationExists(destinationId, token)).thenReturn(true);
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        ReviewResponseDTO response = reviewService.createReview(request, token);

        assertEquals(1L, response.reviewId());
        assertEquals(destinationId, response.destinationId());
        assertEquals(authorUserId, response.authorUserId());
        assertEquals(5, response.rating());
        assertEquals("Excelente destino", response.comment());
        assertEquals(true, response.active());

        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(reviewCaptor.capture());
        Review savedReview = reviewCaptor.getValue();
        assertEquals(destinationId, savedReview.getDestinationId());
        assertEquals(authorUserId, savedReview.getAuthorUserId());
        assertEquals(5, savedReview.getRating());
        assertEquals("Excelente destino", savedReview.getComment());
    }

    @Test
    void createReviewThrowsNotFoundWhenDestinationDoesNotExist() {
        ReviewCreateRequestDTO request = new ReviewCreateRequestDTO(destinationId, 5, "Excelente destino");
        when(loginClient.validateToken("token")).thenReturn(new UserDTO(authorUserId, "victor"));
        when(destinationClient.destinationExists(destinationId, token)).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> reviewService.createReview(request, token));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void getAllReviewsReturnsOnlyActiveReviewsFromRepository() {
        when(loginClient.validateToken("token")).thenReturn(new UserDTO(authorUserId, "victor"));
        when(reviewRepository.findByActiveTrue()).thenReturn(List.of(review));

        List<ReviewResponseDTO> response = reviewService.getAllReviews(token);

        assertEquals(1, response.size());
        assertEquals(1L, response.get(0).reviewId());
        verify(loginClient).validateToken("token");
        verify(reviewRepository).findByActiveTrue();
    }

    @Test
    void getReviewByIdValidatesTokenBeforeReturningReview() {
        when(loginClient.validateToken("token")).thenReturn(new UserDTO(authorUserId, "victor"));
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        ReviewResponseDTO response = reviewService.getReviewById(1L, token);

        assertEquals(1L, response.reviewId());
        verify(loginClient).validateToken("token");
        verify(reviewRepository).findById(1L);
    }

    @Test
    void getReviewsByDestinationValidatesTokenBeforeQueryingRepository() {
        when(loginClient.validateToken("token")).thenReturn(new UserDTO(authorUserId, "victor"));
        when(reviewRepository.findByDestinationIdAndActiveTrue(destinationId)).thenReturn(List.of(review));

        List<ReviewResponseDTO> response = reviewService.getReviewsByDestination(destinationId, token);

        assertEquals(1, response.size());
        verify(loginClient).validateToken("token");
        verify(reviewRepository).findByDestinationIdAndActiveTrue(destinationId);
    }

    @Test
    void getReviewsByAuthorValidatesTokenBeforeQueryingRepository() {
        when(loginClient.validateToken("token")).thenReturn(new UserDTO(authorUserId, "victor"));
        when(reviewRepository.findByAuthorUserIdAndActiveTrue(authorUserId)).thenReturn(List.of(review));

        List<ReviewResponseDTO> response = reviewService.getReviewsByAuthor(authorUserId, token);

        assertEquals(1, response.size());
        verify(loginClient).validateToken("token");
        verify(reviewRepository).findByAuthorUserIdAndActiveTrue(authorUserId);
    }

    @Test
    void updateReviewUpdatesWhenAuthenticatedUserIsAuthor() {
        ReviewUpdateRequestDTO request = new ReviewUpdateRequestDTO(4, "Comentario actualizado");
        when(loginClient.validateToken("token")).thenReturn(new UserDTO(authorUserId, "victor"));
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReviewResponseDTO response = reviewService.updateReview(1L, request, token);

        assertEquals(4, response.rating());
        assertEquals("Comentario actualizado", response.comment());
        assertNotNull(response.updatedAt());
        verify(reviewRepository).save(review);
    }

    @Test
    void updateReviewThrowsForbiddenWhenAuthenticatedUserIsNotAuthor() {
        ReviewUpdateRequestDTO request = new ReviewUpdateRequestDTO(4, "Comentario actualizado");
        when(loginClient.validateToken("token")).thenReturn(new UserDTO(UUID.randomUUID(), "otro"));
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> reviewService.updateReview(1L, request, token));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void deleteReviewMarksReviewAsInactiveWhenAuthenticatedUserIsAuthor() {
        when(loginClient.validateToken("token")).thenReturn(new UserDTO(authorUserId, "victor"));
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        reviewService.deleteReview(1L, token);

        assertFalse(review.getActive());
        assertNotNull(review.getUpdatedAt());
        verify(reviewRepository).save(review);
    }

    @Test
    void getReviewByIdThrowsNotFoundWhenReviewDoesNotExist() {
        when(loginClient.validateToken("token")).thenReturn(new UserDTO(authorUserId, "victor"));
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> reviewService.getReviewById(1L, token));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void getAverageRatingByDestinationCalculatesAverageFromActiveReviews() {
        Review secondReview = new Review();
        secondReview.setRating(3);
        when(loginClient.validateToken("token")).thenReturn(new UserDTO(authorUserId, "victor"));
        when(reviewRepository.findByDestinationIdAndActiveTrue(destinationId))
                .thenReturn(List.of(review, secondReview));

        Double average = reviewService.getAverageRatingByDestination(destinationId, token);

        assertEquals(4.0, average);
        verify(loginClient).validateToken("token");
    }
}
