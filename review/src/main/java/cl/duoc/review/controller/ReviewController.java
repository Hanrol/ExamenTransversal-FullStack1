package cl.duoc.review.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.review.dto.ApiResponse;
import cl.duoc.review.dto.ReviewCreateRequestDTO;
import cl.duoc.review.dto.ReviewResponseDTO;
import cl.duoc.review.dto.ReviewUpdateRequestDTO;
import cl.duoc.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createReview(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody ReviewCreateRequestDTO dto) {
        ReviewResponseDTO response = reviewService.createReview(dto, token);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201, "Reseña creada correctamente", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllReviews() {
        return ResponseEntity.ok(
                new ApiResponse<>(200, "Reseñas obtenidas correctamente", reviewService.getAllReviews()));
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<?>> getReviewById(@PathVariable Long reviewId) {
        return ResponseEntity.ok(
                new ApiResponse<>(200, "Reseña obtenida correctamente", reviewService.getReviewById(reviewId)));
    }

    @GetMapping("/destination/{destinationId}")
    public ResponseEntity<ApiResponse<?>> getReviewsByDestination(@PathVariable UUID destinationId) {
        return ResponseEntity.ok(
                new ApiResponse<>(200, "Reseñas del destino obtenidas correctamente",
                        reviewService.getReviewsByDestination(destinationId)));
    }

    @GetMapping("/user/{authorUserId}")
    public ResponseEntity<ApiResponse<?>> getReviewsByAuthor(@PathVariable UUID authorUserId) {
        return ResponseEntity.ok(
                new ApiResponse<>(200, "Reseñas del usuario obtenidas correctamente",
                        reviewService.getReviewsByAuthor(authorUserId)));
    }

    @GetMapping("/destination/{destinationId}/average-rating")
    public ResponseEntity<ApiResponse<?>> getAverageRatingByDestination(@PathVariable UUID destinationId) {
        return ResponseEntity.ok(
                new ApiResponse<>(200, "Promedio de calificación obtenido correctamente",
                        reviewService.getAverageRatingByDestination(destinationId)));
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<?>> updateReview(
            @PathVariable Long reviewId,
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody ReviewUpdateRequestDTO dto) {
        ReviewResponseDTO response = reviewService.updateReview(reviewId, dto, token);

        return ResponseEntity.ok(
                new ApiResponse<>(200, "Reseña actualizada correctamente", response));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<?>> deleteReview(
            @PathVariable Long reviewId,
            @RequestHeader("Authorization") String token) {
        reviewService.deleteReview(reviewId, token);

        return ResponseEntity.ok(
                new ApiResponse<>(200, "Reseña eliminada correctamente", null));
    }
}