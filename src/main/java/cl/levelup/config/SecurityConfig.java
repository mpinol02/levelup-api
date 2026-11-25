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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain seguridad(
            HttpSecurity http,
            Environment environment
    ) throws Exception {

        boolean perfilLocal = Arrays.asList(
                environment.getActiveProfiles()
        ).contains("local");

        return http

                // Permite la comunicacion entre React y Spring Boot.
                .cors(cors -> {})

                // API REST sin sesiones de servidor.
                .csrf(csrf -> csrf.disable())

                // La autenticacion se realiza mediante JWT.
                .sessionManagement(sesion -> sesion
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(reglas -> {

                    // Permite procesar correctamente las respuestas de error.
                    reglas.dispatcherTypeMatchers(
                            DispatcherType.ERROR
                    ).permitAll();

                    // Permite las solicitudes preflight del navegador.
                    reglas.requestMatchers(
                            HttpMethod.OPTIONS,
                            "/api/v1/**"
                    ).permitAll();

                    // Permite consultar el estado de la API.
                    reglas.requestMatchers(
                            HttpMethod.GET,
                            "/api/v1/estado"
                    ).permitAll();

                    // Permite registro, login, renovacion y logout.
                    // Refresh y logout validan el refresh token recibido.
                    reglas.requestMatchers(
                            HttpMethod.POST,
                            "/api/v1/auth/registro",
                            "/api/v1/auth/login",
                            "/api/v1/auth/refresh",
                            "/api/v1/auth/logout"
                    ).permitAll();

                    // Solo los usuarios autenticados pueden consultar su sesion.
                    reglas.requestMatchers(
                            HttpMethod.GET,
                            "/api/v1/auth/me"
                    ).authenticated();

                    // Permite consultar el perfil del usuario autenticado.
                    reglas.requestMatchers(
                            HttpMethod.GET,
                            "/api/v1/usuarios/me"
                    ).authenticated();

                    // Permite actualizar el propio perfil.
                    // El controlador obtiene el RUN desde el JWT.
                    reglas.requestMatchers(
                            HttpMethod.PUT,
                            "/api/v1/usuarios/me"
                    ).authenticated();

                    // Swagger solamente esta disponible en el perfil local.
                    if (perfilLocal) {
                        reglas.requestMatchers(
                                HttpMethod.GET,
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**"
                        ).permitAll();
                    }

                    // Permite consultar categorias publicamente.
                    reglas.requestMatchers(
                            HttpMethod.GET,
                            "/api/v1/categorias",
                            "/api/v1/categorias/*"
                    ).permitAll();

                    // Solo Administrador puede crear categorias.
                    reglas.requestMatchers(
                            HttpMethod.POST,
                            "/api/v1/categorias"
                    ).hasRole("Administrador");

                    // Solo Administrador puede actualizar categorias.
                    reglas.requestMatchers(
                            HttpMethod.PUT,
                            "/api/v1/categorias/*"
                    ).hasRole("Administrador");

                    // Solo Administrador puede eliminar categorias.
                    reglas.requestMatchers(
                            HttpMethod.DELETE,
                            "/api/v1/categorias/*"
                    ).hasRole("Administrador");

                    // Permite consultar productos publicamente.
                    reglas.requestMatchers(
                            HttpMethod.GET,
                            "/api/v1/productos",
                            "/api/v1/productos/**"
                    ).permitAll();

                    // Solo Administrador puede crear productos.
                    reglas.requestMatchers(
                            HttpMethod.POST,
                            "/api/v1/productos"
                    ).hasRole("Administrador");

                    // Solo Administrador puede actualizar productos.
                    reglas.requestMatchers(
                            HttpMethod.PUT,
                            "/api/v1/productos/*"
                    ).hasRole("Administrador");

                    // Solo Administrador puede eliminar productos.
                    reglas.requestMatchers(
                            HttpMethod.DELETE,
                            "/api/v1/productos/*"
                    ).hasRole("Administrador");

                    // Solo Administrador puede actualizar el stock.
                    reglas.requestMatchers(
                            HttpMethod.PATCH,
                            "/api/v1/productos/*/stock"
                    ).hasRole("Administrador");

                    // Mantiene protegidos los endpoints administrativos.
                    reglas.requestMatchers(
                            HttpMethod.GET,
                            "/api/v1/admin/estado",
                            "/api/v1/admin/usuarios",
                            "/api/v1/admin/reportes/usuarios"
                    ).hasRole("Administrador");

                    // Bloquea cualquier ruta que no tenga permisos definidos.
                    reglas.anyRequest().denyAll();
                })

                // Valida el JWT y convierte su rol en autoridad.
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(convertirRol())))

                .build();
    }

    // Configura los origenes permitidos para desarrollo local.
    @Bean
    CorsConfigurationSource corsConfigurationSource(
            Environment environment
    ) {
        CorsConfiguration configuracion = new CorsConfiguration();

        boolean perfilLocal = Arrays.asList(
                environment.getActiveProfiles()
        ).contains("local");

        // Permite el frontend Vite solamente en el perfil local.
        if (perfilLocal) {
            configuracion.setAllowedOrigins(List.of(
                    "http://localhost:5173"
            ));
        }

        // Metodos HTTP utilizados por la API.
        configuracion.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
        ));

        // Encabezados necesarios para JWT y solicitudes JSON.
        configuracion.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept"
        ));

        // Tiempo de almacenamiento de la respuesta preflight.
        configuracion.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource origenes =
                new UrlBasedCorsConfigurationSource();

        origenes.registerCorsConfiguration(
                "/api/v1/**",
                configuracion
        );

        return origenes;
    }

    // Convierte el rol del JWT en una autoridad de Spring Security.
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

    // Protege las contrasenas mediante BCrypt.
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
