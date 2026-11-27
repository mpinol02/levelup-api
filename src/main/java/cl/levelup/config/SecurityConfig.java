
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
                .cors(cors -> {})
                .csrf(AbstractHttpConfigurer::disable)

                // La autenticación utiliza JWT, sin sesiones del servidor.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // Permite procesar errores y solicitudes CORS.
                        .dispatcherTypeMatchers(
                                DispatcherType.ERROR
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/api/v1/**"
                        ).permitAll()

                        // Estado público de la API.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/estado"
                        ).permitAll()

                        // Autenticación pública.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/auth/registro",
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh",
                                "/api/v1/auth/logout"
                        ).permitAll()

                        // Identidad del usuario autenticado.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/auth/me"
                        ).authenticated()

                        // Consulta y actualización del perfil personal.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/usuarios/me"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/v1/usuarios/me"
                        ).authenticated()

                        // Documentación pública de la API.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Administración de usuarios.
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

                        // Las categorías pueden consultarse públicamente.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/categorias",
                                "/api/v1/categorias/*"
                        ).permitAll()

                        // Solo el administrador modifica categorías.
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

                        // Consulta pública del catálogo de productos.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/productos",
                                "/api/v1/productos/**"
                        ).permitAll()

                        // Administración de productos.
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

                        // Actualización administrativa del stock.
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/v1/productos/*/stock"
                        ).hasRole("Administrador")

                        // Formulario público de contacto.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/contactos"
                        ).permitAll()

                        // Administración de consultas de contacto.
                        .requestMatchers(
                                "/api/v1/admin/contactos",
                                "/api/v1/admin/contactos/**"
                        ).hasRole("Administrador")

                        // Administrador y Vendedor consultan las ordenes.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/admin/ordenes",
                                "/api/v1/admin/ordenes/*"
                        ).hasAnyRole("Administrador", "Vendedor")

                        // Solo el Administrador consulta el historial por RUN.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/admin/ordenes/**"
                        ).hasRole("Administrador")

                        // Estado del panel administrativo.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/admin/estado"
                        ).hasRole("Administrador")

                        // Reportes administrativos.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/admin/reportes/**"
                        ).hasRole("Administrador")

                        // El carrito exige una sesión autenticada.
                        .requestMatchers(
                                "/api/v1/carrito",
                                "/api/v1/carrito/**"
                        ).hasAnyRole("Cliente", "Administrador")

                        // Registra compras de usuarios autenticados.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/compras"
                        ).hasAnyRole("Cliente", "Administrador")

                        // Consulta del historial personal y comprobantes.
                        // El servicio verifica la propiedad de cada orden.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/compras/mis-compras",
                                "/api/v1/compras/*"
                        ).hasAnyRole("Cliente", "Administrador")

                        // Bloquea cualquier ruta no autorizada explícitamente.
                        .anyRequest().denyAll()
                )

                // Valida los tokens JWT y obtiene sus autoridades.
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
    // Ejemplo: Administrador se convierte en ROLE_Administrador.
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

    // Permite la comunicación entre React y Spring Boot.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        // Dirección del frontend durante el desarrollo local.
        configuration.setAllowedOrigins(
                List.of("http://localhost:5173")
        );

        // Métodos HTTP permitidos.
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

        // Permite enviar el JWT y datos JSON.
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

    // Protege las contraseñas mediante hashes BCrypt.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
