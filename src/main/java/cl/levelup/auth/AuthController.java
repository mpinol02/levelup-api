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

    public AuthController(UsuarioService usuarios, AuthService auth, JwtService jwt) {
        this.usuarios = usuarios;
        this.auth = auth;
        this.jwt = jwt;
    }

    // Registra una cuenta y devuelve únicamente sus datos públicos de registro.
    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistroResponse registrar(@Valid @RequestBody RegistroRequest datos) {
        Usuario nuevo = usuarios.registrar(datos);
        return new RegistroResponse(nuevo.getRun(), nuevo.getCorreo(), nuevo.getRol().name());
    }

    // Solo genera el token después de comprobar el correo y la contraseña.
    @PostMapping("/login")
    public LoginResponse ingresar(@Valid @RequestBody LoginRequest datos) {
        Usuario usuario = auth.autenticar(datos);
        String token = jwt.generarToken(usuario);

        return new LoginResponse(token, "Bearer", JwtService.VIGENCIA.toSeconds());
    }

    public record RegistroResponse(String run, String correo, String rol) {}
}