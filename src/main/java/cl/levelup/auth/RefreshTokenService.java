package cl.levelup.auth;

import cl.levelup.config.JwtService;
import cl.levelup.usuario.Usuario;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RefreshTokenService {

    // Duracion maxima de cada token de renovacion.
    public static final Duration VIGENCIA = Duration.ofDays(7);

    private final RefreshTokenRepository repositorio;
    private final JwtService jwt;
    private final SecureRandom generador = new SecureRandom();

    public RefreshTokenService(
            RefreshTokenRepository repositorio,
            JwtService jwt
    ) {
        this.repositorio = repositorio;
        this.jwt = jwt;
    }

    // Genera un token aleatorio y guarda solamente su hash.
    @Transactional
    public String crear(Usuario usuario) {
        byte[] bytes = new byte[32];
        generador.nextBytes(bytes);

        String token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);

        Instant ahora = Instant.now();

        RefreshToken registro = new RefreshToken(
                usuario,
                calcularHash(token),
                ahora,
                ahora.plus(VIGENCIA)
        );

        repositorio.save(registro);

        return token;
    }

    // Renueva la sesion y rota el token dentro de una transaccion.
    @Transactional
    public TokensRenovados renovar(String tokenAnterior) {
        RefreshToken registro = buscarVigente(tokenAnterior);

        Usuario usuario = registro.getUsuario();

        // Invalida el token anterior antes de crear otro.
        registro.revocar(Instant.now());
        repositorio.save(registro);

        // Genera un nuevo token de renovacion para el mismo usuario.
        String nuevoRefreshToken = crear(usuario);

        // Genera un JWT nuevo con el rol actual del usuario.
        String nuevoAccessToken = jwt.generarToken(usuario);

        return new TokensRenovados(
                nuevoAccessToken,
                nuevoRefreshToken,
                JwtService.VIGENCIA.toSeconds()
        );
    }

    // Revoca el token cuando se solicita cerrar la sesion.
    @Transactional
    public void revocar(String token) {
        RefreshToken registro = buscarVigente(token);

        registro.revocar(Instant.now());
        repositorio.save(registro);
    }

    // Consulta el token con bloqueo y verifica que siga vigente.
    private RefreshToken buscarVigente(String token) {
        if (token == null || token.isBlank() || token.length() > 512) {
            throw tokenInvalido();
        }

        RefreshToken registro = repositorio
                .findByTokenHash(calcularHash(token))
                .orElseThrow(this::tokenInvalido);

        if (!registro.estaVigente(Instant.now())) {
            throw tokenInvalido();
        }

        return registro;
    }

    // Convierte el token a un hash SHA-256 hexadecimal.
    private String calcularHash(String token) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] resultado = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(resultado);

        } catch (NoSuchAlgorithmException excepcion) {
            throw new IllegalStateException(
                    "No se pudo calcular el hash del token.",
                    excepcion
            );
        }
    }

    // Responde con 401 cuando el token no puede utilizarse.
    private ResponseStatusException tokenInvalido() {
        return new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Token de renovacion invalido o vencido."
        );
    }

    // Representa los nuevos tokens entregados al renovar la sesion.
    public record TokensRenovados(
            String accessToken,
            String refreshToken,
            long expiresIn
    ) {}
}
