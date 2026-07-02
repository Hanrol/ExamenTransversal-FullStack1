package cl.duoc.review.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import cl.duoc.review.client.DestinationClient;
import cl.duoc.review.client.LoginClient;
import cl.duoc.review.dto.ReviewCreateRequestDTO;
import cl.duoc.review.dto.ReviewResponseDTO;
import cl.duoc.review.dto.ReviewUpdateRequestDTO;
import cl.duoc.review.dto.UserDTO;
import cl.duoc.review.model.Review;
import cl.duoc.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);

    private final ReviewRepository reviewRepository;
    private final DestinationClient destinationClient;
    private final LoginClient loginClient;

    public ReviewResponseDTO createReview(ReviewCreateRequestDTO dto, String token) {

        logger.info("Iniciando creación de reseña para destino ID: {}", dto.destinationId());

        UserDTO authenticatedUser = validateToken(token);

        if (!destinationClient.destinationExists(dto.destinationId(), token)) {
            logger.warn("No se pudo crear la reseña. Destino no existe. ID: {}", dto.destinationId());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El destino indicado no existe");
        }

        Review review = new Review();
        review.setDestinationId(dto.destinationId());
        review.setAuthorUserId(authenticatedUser.id());
        review.setRating(dto.rating());
        review.setComment(dto.comment());
        review.setActive(true);

        Review saved = reviewRepository.save(review);

        logger.info("Reseña creada correctamente con ID: {} por usuario ID: {}",
                saved.getReviewId(), authenticatedUser.id());

        return toResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getAllReviews(String token) {

        logger.info("Listando todas las reseñas activas");
        validateToken(token);

        return reviewRepository.findByActiveTrue()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReviewResponseDTO getReviewById(Long reviewId, String token) {

        logger.info("Buscando reseña por ID: {}", reviewId);
        validateToken(token);

        Review review = findActiveReviewOrThrow(reviewId);

        return toResponseDTO(review);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getReviewsByDestination(UUID destinationId, String token) {

        logger.info("Buscando reseñas activas para destino ID: {}", destinationId);
        validateToken(token);

        return reviewRepository.findByDestinationIdAndActiveTrue(destinationId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getReviewsByAuthor(UUID authorUserId, String token) {

        logger.info("Buscando reseñas activas para autor ID: {}", authorUserId);
        validateToken(token);

        return reviewRepository.findByAuthorUserIdAndActiveTrue(authorUserId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public ReviewResponseDTO updateReview(Long reviewId, ReviewUpdateRequestDTO dto, String token) {

        logger.info("Iniciando actualización de reseña ID: {}", reviewId);

        UserDTO authenticatedUser = validateToken(token);

        Review review = findActiveReviewOrThrow(reviewId);

        if (!review.getAuthorUserId().equals(authenticatedUser.id())) {
            logger.warn("Usuario ID: {} intentó actualizar reseña ID: {} de otro usuario",
                    authenticatedUser.id(), reviewId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el autor puede actualizar esta reseña");
        }

        review.setRating(dto.rating());
        review.setComment(dto.comment());
        review.setUpdatedAt(LocalDateTime.now());

        Review updated = reviewRepository.save(review);

        logger.info("Reseña actualizada correctamente. ID: {}", updated.getReviewId());

        return toResponseDTO(updated);
    }

    public void deleteReview(Long reviewId, String token) {

        logger.info("Iniciando eliminación lógica de reseña ID: {}", reviewId);

        UserDTO authenticatedUser = validateToken(token);

        Review review = findActiveReviewOrThrow(reviewId);

        if (!review.getAuthorUserId().equals(authenticatedUser.id())) {
            logger.warn("Usuario ID: {} intentó eliminar reseña ID: {} de otro usuario",
                    authenticatedUser.id(), reviewId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el autor puede eliminar esta reseña");
        }

        review.setActive(false);
        review.setUpdatedAt(LocalDateTime.now());

        reviewRepository.save(review);

        logger.info("Reseña eliminada lógicamente. ID: {}", reviewId);
    }

    @Transactional(readOnly = true)
    public Double getAverageRatingByDestination(UUID destinationId, String token) {

        logger.info("Calculando promedio de calificación para destino ID: {}", destinationId);
        validateToken(token);

        List<Review> reviews = reviewRepository.findByDestinationIdAndActiveTrue(destinationId);

        if (reviews.isEmpty()) {
            logger.info("No existen reseñas activas para el destino ID: {}", destinationId);
            return 0.0;
        }

        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

    private Review findActiveReviewOrThrow(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> {
                    logger.warn("Reseña no encontrada. ID: {}", reviewId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Reseña no encontrada");
                });

        if (!Boolean.TRUE.equals(review.getActive())) {
            logger.warn("Reseña inactiva o eliminada. ID: {}", reviewId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reseña no encontrada o inactiva");
        }

        return review;
    }

    private UserDTO validateToken(String token) {
        if (token == null || token.isBlank()) {
            logger.warn("Token JWT no proporcionado");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token JWT no proporcionado");
        }

        String cleanToken = token.replace("Bearer ", "");

        UserDTO user = loginClient.validateToken(cleanToken);

        if (user == null || user.id() == null) {
            logger.warn("Token JWT inválido o sin usuario asociado");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token JWT inválido o expirado");
        }

        return user;
    }

    private ReviewResponseDTO toResponseDTO(Review review) {
        return new ReviewResponseDTO(
                review.getReviewId(),
                review.getDestinationId(),
                review.getAuthorUserId(),
                review.getRating(),
                review.getComment(),
                review.getActive(),
                review.getCreatedAt(),
                review.getUpdatedAt());
    }
}
