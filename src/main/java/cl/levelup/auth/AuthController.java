package cl.levelup.auth;

import cl.levelup.config.JwtService;
import cl.levelup.usuario.RegistroRequest;
import cl.levelup.usuario.Usuario;
import cl.levelup.usuario.UsuarioService;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UsuarioService usuarios;
    private final AuthService auth;
    private final JwtService jwt;
    private final RefreshTokenService refreshTokens;

    public AuthController(
            UsuarioService usuarios,
            AuthService auth,
            JwtService jwt,
            RefreshTokenService refreshTokens
    ) {
        this.usuarios = usuarios;
        this.auth = auth;
        this.jwt = jwt;
        this.refreshTokens = refreshTokens;
    }

    // Registra una cuenta con el rol Cliente.
    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistroResponse registrar(
            @Valid @RequestBody RegistroRequest datos
    ) {
        Usuario nuevo = usuarios.registrar(datos);

        return new RegistroResponse(
                nuevo.getRun(),
                nuevo.getCorreo(),
                nuevo.getRol().name()
        );
    }

    // Comprueba las credenciales y entrega ambos tokens.
    @PostMapping("/login")
    public LoginResponse ingresar(
            @Valid @RequestBody LoginRequest datos
    ) {
        Usuario usuario = auth.autenticar(datos);

        String accessToken = jwt.generarToken(usuario);
        String refreshToken = refreshTokens.crear(usuario);

        return new LoginResponse(
                accessToken,
                "Bearer",
                JwtService.VIGENCIA.toSeconds(),
                refreshToken
        );
    }

    // Renueva la sesion y reemplaza el refresh token anterior.
    @PostMapping("/refresh")
    public RefreshTokenService.TokensRenovados renovar(
            @Valid @RequestBody RenovacionRequest datos
    ) {
        return refreshTokens.renovar(datos.refreshToken());
    }

    // Revoca el refresh token y finaliza la sesion renovable.
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cerrarSesion(
            @Valid @RequestBody RenovacionRequest datos
    ) {
        refreshTokens.revocar(datos.refreshToken());
    }

    // Datos publicos devueltos durante el registro.
    public record RegistroResponse(
            String run,
            String correo,
            String rol
    ) {}
}
