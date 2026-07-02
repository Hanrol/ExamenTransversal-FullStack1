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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Review Controller", description = "Endpoints para crear, consultar, actualizar y eliminar reseñas de destinos.")
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "Crear reseña", description = "Crea una reseña asociada a un destino existente. El usuario autor se obtiene desde el token JWT.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Reseña creada correctamente", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Token JWT inválido o ausente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Destino no encontrado")
    })
    public ResponseEntity<ApiResponse<?>> createReview(
            @Parameter(description = "Token JWT con formato Bearer") @RequestHeader("Authorization") String token,
            @Valid @RequestBody ReviewCreateRequestDTO dto) {
        ReviewResponseDTO response = reviewService.createReview(dto, token);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201, "Reseña creada correctamente", response));
    }

    @GetMapping
    @Operation(summary = "Listar reseñas", description = "Obtiene todas las reseñas activas registradas.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reseñas obtenidas correctamente", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Token JWT inválido o ausente")
    })
    public ResponseEntity<ApiResponse<?>> getAllReviews(
            @Parameter(description = "Token JWT con formato Bearer") @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(
                new ApiResponse<>(200, "Reseñas obtenidas correctamente",
                        reviewService.getAllReviews(token)));
    }

    @GetMapping("/{reviewId}")
    @Operation(summary = "Obtener reseña por ID", description = "Busca una reseña activa por su identificador.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reseña obtenida correctamente", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Token JWT inválido o ausente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Reseña no encontrada")
    })
    public ResponseEntity<ApiResponse<?>> getReviewById(
            @Parameter(description = "ID numérico de la reseña", example = "1") @PathVariable Long reviewId,
            @Parameter(description = "Token JWT con formato Bearer") @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(
                new ApiResponse<>(200, "Reseña obtenida correctamente",
                        reviewService.getReviewById(reviewId, token)));
    }

    @GetMapping("/destination/{destinationId}")
    @Operation(summary = "Listar reseñas por destino", description = "Obtiene las reseñas activas asociadas a un destino.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reseñas del destino obtenidas correctamente", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "ID de destino inválido"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Token JWT inválido o ausente")
    })
    public ResponseEntity<ApiResponse<?>> getReviewsByDestination(
            @Parameter(description = "UUID del destino", example = "3b3f3937-73c9-11f1-b577-424dc013a170") @PathVariable UUID destinationId,
            @Parameter(description = "Token JWT con formato Bearer") @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(
                new ApiResponse<>(200, "Reseñas del destino obtenidas correctamente",
                        reviewService.getReviewsByDestination(destinationId, token)));
    }

    @GetMapping("/user/{authorUserId}")
    @Operation(summary = "Listar reseñas por usuario", description = "Obtiene las reseñas activas creadas por un usuario.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reseñas del usuario obtenidas correctamente", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "ID de usuario inválido"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Token JWT inválido o ausente")
    })
    public ResponseEntity<ApiResponse<?>> getReviewsByAuthor(
            @Parameter(description = "UUID del usuario autor", example = "dafff0e9-509a-46d2-86a4-0954a0ad5f6b") @PathVariable UUID authorUserId,
            @Parameter(description = "Token JWT con formato Bearer") @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(
                new ApiResponse<>(200, "Reseñas del usuario obtenidas correctamente",
                        reviewService.getReviewsByAuthor(authorUserId, token)));
    }

    @GetMapping("/destination/{destinationId}/average-rating")
    @Operation(summary = "Obtener promedio de calificación", description = "Calcula el promedio de notas activas para un destino.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Promedio de calificación obtenido correctamente", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "ID de destino inválido"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Token JWT inválido o ausente")
    })
    public ResponseEntity<ApiResponse<?>> getAverageRatingByDestination(
            @Parameter(description = "UUID del destino", example = "3b3f3937-73c9-11f1-b577-424dc013a170") @PathVariable UUID destinationId,
            @Parameter(description = "Token JWT con formato Bearer") @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(
                new ApiResponse<>(200, "Promedio de calificación obtenido correctamente",
                        reviewService.getAverageRatingByDestination(destinationId, token)));
    }

    @PutMapping("/{reviewId}")
    @Operation(summary = "Actualizar reseña", description = "Actualiza una reseña activa. Solo el usuario autor puede modificarla.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reseña actualizada correctamente", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Token JWT inválido o ausente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "El usuario no es autor de la reseña"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Reseña no encontrada")
    })
    public ResponseEntity<ApiResponse<?>> updateReview(
            @Parameter(description = "ID numérico de la reseña", example = "1") @PathVariable Long reviewId,
            @Parameter(description = "Token JWT con formato Bearer") @RequestHeader("Authorization") String token,
            @Valid @RequestBody ReviewUpdateRequestDTO dto) {
        ReviewResponseDTO response = reviewService.updateReview(reviewId, dto, token);

        return ResponseEntity.ok(
                new ApiResponse<>(200, "Reseña actualizada correctamente", response));
    }

    @DeleteMapping("/{reviewId}")
    @Operation(summary = "Eliminar reseña", description = "Realiza una eliminación lógica de la reseña. Solo el usuario autor puede eliminarla.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reseña eliminada correctamente", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Token JWT inválido o ausente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "El usuario no es autor de la reseña"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Reseña no encontrada")
    })
    public ResponseEntity<ApiResponse<?>> deleteReview(
            @Parameter(description = "ID numérico de la reseña", example = "1") @PathVariable Long reviewId,
            @Parameter(description = "Token JWT con formato Bearer") @RequestHeader("Authorization") String token) {
        reviewService.deleteReview(reviewId, token);

        return ResponseEntity.ok(
                new ApiResponse<>(200, "Reseña eliminada correctamente", null));
    }
}
