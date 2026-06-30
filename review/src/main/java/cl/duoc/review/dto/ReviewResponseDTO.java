package cl.duoc.review.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewResponseDTO(
        Long reviewId,
        UUID destinationId,
        UUID authorUserId,
        Integer rating,
        String comment,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}