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

        String registro = "/api/v1/auth/registro";
        String login = "/api/v1/auth/login";

        // Swagger público únicamente en el perfil local.
        boolean perfilLocal = Arrays.asList(
                environment.getActiveProfiles()
        ).contains("local");

        return http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(registro, login))

                .sessionManagement(sesion -> sesion
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(reglas -> {

                    reglas.dispatcherTypeMatchers(
                            DispatcherType.ERROR
                    ).permitAll();

                    reglas.requestMatchers(
                            HttpMethod.GET,
                            "/api/v1/estado"
                    ).permitAll();

                    reglas.requestMatchers(
                            HttpMethod.POST,
                            registro,
                            login
                    ).permitAll();

                    reglas.requestMatchers(
                            HttpMethod.GET,
                            "/api/v1/auth/me"
                    ).authenticated();

                    // Permitir documentación solo durante desarrollo local.
                    if (perfilLocal) {
                        reglas.requestMatchers(
                                HttpMethod.GET,
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**"
                        ).permitAll();
                    }

                    // Acceso exclusivo del Administrador.
                    reglas.requestMatchers(
                            HttpMethod.GET,
                            "/api/v1/admin/estado",
                            "/api/v1/admin/usuarios",
                            "/api/v1/admin/reportes/usuarios"
                    ).hasRole("Administrador");

                    // Bloquea cualquier ruta no autorizada.
                    reglas.anyRequest().denyAll();
                })

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
