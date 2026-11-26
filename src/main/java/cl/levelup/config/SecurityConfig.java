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
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {})
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth

                        // Permite procesar errores y solicitudes de verificación CORS.
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/api/v1/**").permitAll()

                        // Estado público de la API.
                        .requestMatchers(HttpMethod.GET, "/api/v1/estado").permitAll()

                        // Autenticación.
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/auth/registro",
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh",
                                "/api/v1/auth/logout"
                        ).permitAll()

                        // Identidad y perfil del usuario autenticado.
                        .requestMatchers(HttpMethod.GET, "/api/v1/auth/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/usuarios/me").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/usuarios/me").authenticated()

                        // Documentación de la API.
                        .requestMatchers(HttpMethod.GET,
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Administración de usuarios.
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/admin/usuarios",
                                "/api/v1/admin/usuarios/*"
                        ).hasRole("Administrador")
                        .requestMatchers(HttpMethod.POST, "/api/v1/admin/usuarios")
                                .hasRole("Administrador")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/admin/usuarios/*")
                                .hasRole("Administrador")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/admin/usuarios/*")
                                .hasRole("Administrador")

                        // Todos pueden consultar categorías; solo el administrador las modifica.
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/categorias",
                                "/api/v1/categorias/*"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/categorias")
                                .hasRole("Administrador")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/categorias/*")
                                .hasRole("Administrador")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/categorias/*")
                                .hasRole("Administrador")

                        // Catálogo público y administración de productos.
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/productos",
                                "/api/v1/productos/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/productos")
                                .hasRole("Administrador")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/productos/*")
                                .hasRole("Administrador")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/productos/*")
                                .hasRole("Administrador")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/productos/*/stock")
                                .hasRole("Administrador")

                        // Formulario público de contacto y atención administrativa.
                        .requestMatchers(HttpMethod.POST, "/api/v1/contactos").permitAll()
                        .requestMatchers(
                                "/api/v1/admin/contactos",
                                "/api/v1/admin/contactos/**"
                        ).hasRole("Administrador")

                        // Solo el administrador puede listar órdenes y consultar su detalle.
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/admin/ordenes",
                                "/api/v1/admin/ordenes/*"
                        ).hasRole("Administrador")

                        // Estado y reportes del panel administrativo.
                        .requestMatchers(HttpMethod.GET, "/api/v1/admin/estado")
                                .hasRole("Administrador")
                        .requestMatchers(HttpMethod.GET, "/api/v1/admin/reportes/**")
                                .hasRole("Administrador")

                        // Un usuario autenticado puede registrar una compra.
                        .requestMatchers(HttpMethod.POST, "/api/v1/compras").authenticated()

                        // Las rutas sin una regla explícita quedan bloqueadas.
                        .anyRequest().denyAll()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                );

        return http.build();
    }

    // Convierte el rol del JWT en una autoridad, por ejemplo ROLE_Administrador.
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter =
                new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("rol");
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter authenticationConverter =
                new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return authenticationConverter;
    }

    // Permite que la tienda React local haga peticiones al backend.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        );
        configuration.setAllowedHeaders(
                List.of("Authorization", "Content-Type", "Accept")
        );
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/v1/**", configuration);
        return source;
    }

    // Guarda las contraseñas como hashes BCrypt.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}