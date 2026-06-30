package cl.duoc.review.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.review.model.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByDestinationIdAndActiveTrue(Long destinationId);

    List<Review> findByAuthorUserIdAndActiveTrue(Long authorUserId);

    List<Review> findByActiveTrue();
}