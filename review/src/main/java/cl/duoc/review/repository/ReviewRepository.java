package cl.duoc.review.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.review.model.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByDestinationIdAndActiveTrue(UUID destinationId);

    List<Review> findByAuthorUserIdAndActiveTrue(UUID authorUserId);

    List<Review> findByActiveTrue();
}