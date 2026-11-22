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

    // Define las rutas públicas y las rutas protegidas.
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
                        .dispatcherTypeMatchers(DispatcherType.ERROR)
                        .permitAll()

                        // Estado público de la API.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/estado"
                        ).permitAll()

                        // Registro e inicio de sesión.
                        .requestMatchers(
                                HttpMethod.POST,
                                registro,
                                login
                        ).permitAll()

                        // Información del usuario autenticado.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/auth/me"
                        ).authenticated()

                        // Rutas exclusivas del Administrador.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/admin/estado",
                                "/api/v1/admin/usuarios",
                                "/api/v1/admin/reportes/usuarios"
                        ).hasRole("Administrador")

                        // Bloquea las rutas no autorizadas.
                        .anyRequest().denyAll()
                )

                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(convertirRol())))

                .build();
    }

    // Convierte el rol del JWT a una autoridad de Spring Security.
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

    // Codificación de contraseñas mediante BCrypt.
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
