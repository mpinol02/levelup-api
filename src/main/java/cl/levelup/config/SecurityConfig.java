package cl.levelup.config;

import java.util.List;

import jakarta.servlet.DispatcherType;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                // Permite la comunicacion entre React y Spring Boot.
                .cors(cors -> {})

                // La API utiliza JWT y no sesiones tradicionales.
                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // Permite procesar errores internos.
                        .dispatcherTypeMatchers(
                                DispatcherType.ERROR
                        ).permitAll()

                        // Permite solicitudes CORS de verificacion.
                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/api/v1/**"
                        ).permitAll()

                        // Estado publico de la API.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/estado"
                        ).permitAll()

                        // Endpoints publicos de autenticacion.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/auth/registro",
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh",
                                "/api/v1/auth/logout"
                        ).permitAll()

                        // Consulta de la identidad autenticada.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/auth/me"
                        ).authenticated()

                        // Consulta del perfil personal.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/usuarios/me"
                        ).authenticated()

                        // Actualizacion del perfil personal.
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/v1/usuarios/me"
                        ).authenticated()

                        // Documentacion Swagger y OpenAPI.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Administracion de usuarios.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/admin/usuarios",
                                "/api/v1/admin/usuarios/*"
                        ).hasRole("Administrador")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/admin/usuarios"
                        ).hasRole("Administrador")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/v1/admin/usuarios/*"
                        ).hasRole("Administrador")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/v1/admin/usuarios/*"
                        ).hasRole("Administrador")

                        // Consulta publica de categorias.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/categorias",
                                "/api/v1/categorias/*"
                        ).permitAll()

                        // Administracion de categorias.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/categorias"
                        ).hasRole("Administrador")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/v1/categorias/*"
                        ).hasRole("Administrador")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/v1/categorias/*"
                        ).hasRole("Administrador")

                        // Consulta publica de productos.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/productos",
                                "/api/v1/productos/**"
                        ).permitAll()

                        // Administracion de productos.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/productos"
                        ).hasRole("Administrador")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/v1/productos/*"
                        ).hasRole("Administrador")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/v1/productos/*"
                        ).hasRole("Administrador")

                        // Actualizacion del stock.
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/v1/productos/*/stock"
                        ).hasRole("Administrador")

                        // Formulario publico de contacto.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/contactos"
                        ).permitAll()

                        // Administracion de contacto y soporte.
                        .requestMatchers(
                                "/api/v1/admin/contactos",
                                "/api/v1/admin/contactos/**"
                        ).hasRole("Administrador")

                        // Estado del panel administrativo.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/admin/estado"
                        ).hasRole("Administrador")

                        // Reportes administrativos de usuarios,
                        // contacto y futuros indicadores.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/admin/reportes/**"
                        ).hasRole("Administrador")

                        // Bloquea cualquier ruta no autorizada.
                        .anyRequest().denyAll()
                )

                // Configura la autenticacion mediante JWT.
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(
                                        jwtAuthenticationConverter()
                                )
                        )
                );

        return http.build();
    }

    // Convierte el rol del JWT en una autoridad de Spring Security.
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter authoritiesConverter =
                new JwtGrantedAuthoritiesConverter();

        authoritiesConverter.setAuthoritiesClaimName("rol");

        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter authenticationConverter =
                new JwtAuthenticationConverter();

        authenticationConverter.setJwtGrantedAuthoritiesConverter(
                authoritiesConverter
        );

        return authenticationConverter;
    }

    // Configura los permisos de comunicacion con React.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of("http://localhost:5173")
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type",
                        "Accept"
                )
        );

        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/api/v1/**",
                configuration
        );

        return source;
    }

    // Protege las contrasenas mediante BCrypt.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
