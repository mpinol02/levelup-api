package cl.levelup.config;

import jakarta.servlet.DispatcherType;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Arrays;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain seguridad(
            HttpSecurity http,
            Environment environment) throws Exception {

        boolean perfilLocal = Arrays.asList(
                environment.getActiveProfiles()
        ).contains("local");

        return http

                // API REST sin sesiones, autenticada mediante JWT.
                .csrf(csrf -> csrf.disable())

                .sessionManagement(sesion -> sesion
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(reglas -> {

                    // Permite procesar respuestas de error.
                    reglas.dispatcherTypeMatchers(
                            DispatcherType.ERROR
                    ).permitAll();

                    // Estado público de la API.
                    reglas.requestMatchers(
                            HttpMethod.GET,
                            "/api/v1/estado"
                    ).permitAll();

                    // Registro e inicio de sesión.
                    reglas.requestMatchers(
                            HttpMethod.POST,
                            "/api/v1/auth/registro",
                            "/api/v1/auth/login"
                    ).permitAll();

                    // Información del usuario autenticado.
                    reglas.requestMatchers(
                            HttpMethod.GET,
                            "/api/v1/auth/me"
                    ).authenticated();

                    // Swagger disponible solo en el perfil local.
                    if (perfilLocal) {
                        reglas.requestMatchers(
                                HttpMethod.GET,
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**"
                        ).permitAll();
                    }

                    // Consultas públicas de categorías.
                    reglas.requestMatchers(
                            HttpMethod.GET,
                            "/api/v1/categorias",
                            "/api/v1/categorias/*"
                    ).permitAll();

                    // Creación de categorías: solo Administrador.
                    reglas.requestMatchers(
                            HttpMethod.POST,
                            "/api/v1/categorias"
                    ).hasRole("Administrador");

                    // Actualización de categorías: solo Administrador.
                    reglas.requestMatchers(
                            HttpMethod.PUT,
                            "/api/v1/categorias/*"
                    ).hasRole("Administrador");

                    // Eliminación de categorías: solo Administrador.
                    reglas.requestMatchers(
                            HttpMethod.DELETE,
                            "/api/v1/categorias/*"
                    ).hasRole("Administrador");

                    // Endpoints administrativos existentes.
                    reglas.requestMatchers(
                            HttpMethod.GET,
                            "/api/v1/admin/estado",
                            "/api/v1/admin/usuarios",
                            "/api/v1/admin/reportes/usuarios"
                    ).hasRole("Administrador");

                    // Todas las demás rutas permanecen bloqueadas.
                    reglas.anyRequest().denyAll();
                })

                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(convertirRol())))

                .build();
    }

    // Convierte el rol del JWT en una autoridad Spring Security.
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

    // Codifica las contraseñas mediante BCrypt.
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
