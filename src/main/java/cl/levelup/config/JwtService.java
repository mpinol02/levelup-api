package cl.levelup.config;

import java.time.Duration;
import java.time.Instant;

import cl.levelup.usuario.Usuario;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    static final String VERSION_SEGURIDAD = "version_seguridad";
    public static final String EMISOR = "levelup-api";
    public static final Duration VIGENCIA = Duration.ofMinutes(15);

    private final JwtEncoder encoder;

    public JwtService(JwtEncoder encoder) {
        this.encoder = encoder;
    }

    // Genera un token para una cuenta que ya pasó la validación de contraseña.
    public String generarToken(Usuario usuario) {
        Instant ahora = Instant.now();

        JwtClaimsSet datos = JwtClaimsSet.builder()
                .issuer(EMISOR)
                .subject(usuario.getRun())
                .issuedAt(ahora)
                .expiresAt(ahora.plus(VIGENCIA))
                .claim("rol", usuario.getRol().name())
                .claim(VERSION_SEGURIDAD, usuario.getVersionSeguridad())
                .build();

        // Firma con HS256 usando la clave que prepara JwtConfig.
        JwsHeader cabecera = JwsHeader.with(MacAlgorithm.HS256).build();

        return encoder.encode(JwtEncoderParameters.from(cabecera, datos))
                .getTokenValue();
    }
}