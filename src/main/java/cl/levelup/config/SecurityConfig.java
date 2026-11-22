
package cl.levelup.config;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // Define qué rutas son públicas y cuáles requieren autenticación.
    @Bean
    SecurityFilterChain seguridad(HttpSecurity http) throws Exception {

        String registro = "/api/v1/auth/registro";
        String login = "/api/v1/auth/login";

        return http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(registro, login))
                .sessionManagement(sesion -> sesion
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(reglas -> reglas

                        // Permite procesar respuestas de error.
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()

                        // Endpoint público del estado de la API.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/estado"
                        ).permitAll()

                        // Registro e inicio de sesión públicos.
                        .requestMatchers(
                                HttpMethod.POST,
                                registro,
                                login
                        ).permitAll()

                        // Consulta del usuario autenticado.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/auth/me"
                        ).authenticated()

                        // Endpoints exclusivos del Administrador.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/admin/estado",
                                "/api/v1/admin/usuarios"
                        ).hasRole("Administrador")

                        // Bloquea los endpoints no autorizados.
                        .anyRequest().denyAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(convertirRol())))
                .build();
    }

    // Convierte el rol del JWT a una autoridad de Spring Security.
    // Ejemplo: "Administrador" -> "ROLE_Administrador".
    private JwtAuthenticationConverter convertirRol() {

        JwtGrantedAuthoritiesConverter roles =
                new JwtGrantedAuthoritiesConverter();

        roles.setAuthoritiesClaimName("rol");
        roles.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter conversor =
                new JwtAuthenticationConverter();

        conversor.setJwtGrantedAuthoritiesConverter(roles);

        return conversor;
    }

    // Cifra y verifica contraseñas mediante BCrypt.
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
