package cl.duoc.review.client;

import cl.duoc.review.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
@RequiredArgsConstructor
public class LoginClient {

    private final WebClient.Builder webClientBuilder;

    public boolean userExists(Long userId) {
        try {
            ApiResponse<Object> response = webClientBuilder.build()
                    .get()
                    .uri("http://login/api/v1/users/{userId}", userId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<Object>>() {
                    })
                    .block();

            return response != null && response.getData() != null;

        } catch (WebClientResponseException.NotFound ex) {
            return false;
        } catch (WebClientResponseException ex) {
            throw new RuntimeException("Error al consultar Login Service");
        } catch (Exception ex) {
            throw new RuntimeException("No se pudo conectar con Login Service");
        }
    }

    public boolean validateToken(String token) {
        try {
            ApiResponse<Boolean> response = webClientBuilder.build()
                    .get()
                    .uri("http://login/api/v1/auth/validate?token={token}", token)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<Boolean>>() {
                    })
                    .block();

            return response != null && Boolean.TRUE.equals(response.getData());

        } catch (WebClientResponseException ex) {
            return false;
        } catch (Exception ex) {
            throw new RuntimeException("No se pudo validar el token en Login Service");
        }
    }
}