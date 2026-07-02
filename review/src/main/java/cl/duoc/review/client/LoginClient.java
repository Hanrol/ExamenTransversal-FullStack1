package cl.duoc.review.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import cl.duoc.review.dto.ApiResponse;
import cl.duoc.review.dto.UserDTO;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LoginClient {

    private final WebClient.Builder webClientBuilder;

    public UserDTO validateToken(String token) {
        try {
            ApiResponse<UserDTO> response = webClientBuilder.build()
                    .get()
                    .uri("http://login/api/v1/users/validate?token={token}", token)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<UserDTO>>() {
                    })
                    .block();

            if (response == null || response.getCode() != 200 || response.getData() == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token JWT inválido o expirado");
            }

            return response.getData();

        } catch (WebClientResponseException.Unauthorized ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token JWT inválido o expirado");
        } catch (WebClientResponseException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Error al validar token en Login Service");
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "No se pudo conectar con Login Service");
        }
    }
}
