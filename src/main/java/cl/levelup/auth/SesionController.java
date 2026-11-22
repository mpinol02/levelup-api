package cl.levelup.auth;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SesionController {

    // Devuelve la identidad de quien hizo la petición.
    @GetMapping("/api/v1/auth/me")
    public SesionResponse obtenerSesion(@AuthenticationPrincipal Jwt jwt) {
        return new SesionResponse(
                jwt.getSubject(),
                jwt.getClaimAsString("rol")
        );
    }

    public record SesionResponse(String run, String rol) {}
}