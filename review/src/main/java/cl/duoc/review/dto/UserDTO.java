package cl.duoc.review.dto;

import java.util.UUID;

public record UserDTO(
        UUID id,
        String username
) {
}