package cl.levelup;

import cl.levelup.auth.RefreshToken;
import cl.levelup.auth.RefreshTokenRepository;
import cl.levelup.auth.RefreshTokenService;
import cl.levelup.auth.RefreshTokenService.TokensRenovados;
import cl.levelup.usuario.RegistroRequest;
import cl.levelup.usuario.Usuario;
import cl.levelup.usuario.UsuarioService;

import java.time.Instant;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
class RefreshTokenTest {

    @Autowired
    private UsuarioService usuarios;

    @Autowired
    private RefreshTokenService refreshTokens;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private Usuario usuario;

    // Prepara una cuenta de prueba antes de cada caso.
    @BeforeEach
    void prepararCuenta() {
        usuario = usuarios.registrar(new RegistroRequest(
                "123456785",
                "Mario",
                "Prueba JWT",
                "mario.jwt.prueba@gmail.com",
                LocalDate.of(2000, 1, 1),
                "Metropolitana",
                "Maipu",
                "Calle de prueba 123",
                "clave123"
        ));
    }

    // Comprueba que se genere un refresh token valido.
    @Test
    void generaRefreshTokenCorrectamente() {
        String token = refreshTokens.crear(usuario);

        assertThat(token).isNotBlank();
        assertThat(token).hasSize(43);
    }

    // Comprueba que un token valido permita renovar la sesion.
    @Test
    void renuevaLaSesionCorrectamente() {
        String tokenOriginal = refreshTokens.crear(usuario);

        TokensRenovados resultado =
                refreshTokens.renovar(tokenOriginal);

        assertThat(resultado.accessToken()).isNotBlank();
        assertThat(resultado.refreshToken()).isNotBlank();
        assertThat(resultado.refreshToken())
                .isNotEqualTo(tokenOriginal);
        assertThat(resultado.expiresIn()).isEqualTo(900);
    }

    // Comprueba que un token consumido no pueda reutilizarse.
    @Test
    void rechazaRefreshTokenReutilizado() {
        String tokenOriginal = refreshTokens.crear(usuario);

        refreshTokens.renovar(tokenOriginal);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> refreshTokens.renovar(tokenOriginal)
        );

        assertThat(error.getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // Comprueba que logout impida nuevas renovaciones.
    @Test
    void rechazaRefreshTokenRevocado() {
        String token = refreshTokens.crear(usuario);

        refreshTokens.revocar(token);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> refreshTokens.renovar(token)
        );

        assertThat(error.getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // Comprueba que un token inexistente sea rechazado.
    @Test
    void rechazaRefreshTokenInexistente() {
        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> refreshTokens.renovar("token-inexistente")
        );

        assertThat(error.getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // Comprueba que una solicitud sin token sea rechazada.
    @Test
    void rechazaRefreshTokenVacio() {
        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> refreshTokens.renovar("")
        );

        assertThat(error.getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // Comprueba que un token vencido no pueda renovar la sesion.
    @Test
    void rechazaRefreshTokenVencido() {
        String token = refreshTokens.crear(usuario);

        // Recupera el registro creado para esta prueba.
        RefreshToken registro = refreshTokenRepository
                .findAll()
                .stream()
                .findFirst()
                .orElseThrow();

        // Simula que el token vencio hace un minuto.
        ReflectionTestUtils.setField(
                registro,
                "venceEn",
                Instant.now().minusSeconds(60)
        );

        // Guarda la fecha vencida en la base de datos H2.
        refreshTokenRepository.saveAndFlush(registro);

        // Comprueba que el servicio rechace el token vencido.
        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> refreshTokens.renovar(token)
        );

        assertThat(error.getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
