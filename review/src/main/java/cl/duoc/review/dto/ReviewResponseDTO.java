package cl.duoc.review.dto;

import java.time.LocalDateTime;

public record ReviewResponseDTO(
        Long reviewId,
        Long destinationId,
        Long authorUserId,
        Integer rating,
        String comment,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}