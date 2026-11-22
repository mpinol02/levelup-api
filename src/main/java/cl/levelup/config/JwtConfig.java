package cl.levelup.config;

import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
public class JwtConfig {

    // En local se genera una clave nueva cada vez que arranca el backend.
    @Bean
    @Profile("local")
    SecretKey claveJwtLocal() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    // Fuera de local, la clave se obtiene desde el entorno.
    @Bean
    @Profile("!local")
    SecretKey claveJwtConfigurada(
            @Value("${JWT_SECRET_BASE64}") String secretoBase64) {

        byte[] bytes = Base64.getDecoder().decode(secretoBase64);

        if (bytes.length < 32) {
            throw new IllegalArgumentException(
                    "JWT_SECRET_BASE64 debe contener al menos 32 bytes al decodificarse."
            );
        }

        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    // Firma los tokens que entrega el login.
    @Bean
    JwtEncoder jwtEncoder(SecretKey claveJwt) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(claveJwt));
    }

    // Verifica los tokens que llegan en las peticiones protegidas.
    @Bean
    JwtDecoder jwtDecoder(SecretKey claveJwt) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(claveJwt)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer(JwtService.EMISOR)
        );

        return decoder;
    }
}