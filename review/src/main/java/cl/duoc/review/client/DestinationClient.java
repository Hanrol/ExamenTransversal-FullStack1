package cl.duoc.review.client;

import cl.duoc.review.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
@RequiredArgsConstructor
public class DestinationClient {

    private final WebClient.Builder webClientBuilder;

    public boolean destinationExists(Long destinationId) {
        try {
            ApiResponse<Object> response = webClientBuilder.build()
                    .get()
                    .uri("http://destination/api/v1/destinations/{destinationId}", destinationId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<Object>>() {
                    })
                    .block();

            return response != null && response.getData() != null;

        } catch (WebClientResponseException.NotFound ex) {
            return false;
        } catch (WebClientResponseException ex) {
            throw new RuntimeException("Error al consultar Destination Service");
        } catch (Exception ex) {
            throw new RuntimeException("No se pudo conectar con Destination Service");
        }
    }
}