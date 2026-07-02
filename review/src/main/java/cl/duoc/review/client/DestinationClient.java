package cl.duoc.review.client;

import java.util.UUID;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import cl.duoc.review.dto.ApiResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DestinationClient {

    private final WebClient.Builder webClientBuilder;

    public boolean destinationExists(UUID destinationId, String token) {
        try {
            ApiResponse<Boolean> response = webClientBuilder.build()
                    .get()
                    .uri("http://destination/api/v1/destination/destinations/exists?id={id}", destinationId)
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<Boolean>>() {
                    })
                    .block();

            return response != null && Boolean.TRUE.equals(response.getData());

        } catch (WebClientResponseException.NotFound ex) {
            return false;
        } catch (WebClientResponseException.Unauthorized ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token no autorizado al consultar Destination Service");
        } catch (WebClientResponseException.Forbidden ex) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado al consultar Destination Service");
        } catch (WebClientResponseException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Error al consultar Destination Service: " + ex.getStatusCode());
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo conectar con Destination Service");
        }
    }
}
