package cl.levelup.auth;

import java.util.Locale;

import cl.levelup.usuario.Usuario;
import cl.levelup.usuario.UsuarioRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

@Service
@Validated
public class AuthService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
    }

    // Comprueba las credenciales sin modificar los datos del usuario.
    @Transactional(readOnly = true)
    public Usuario autenticar(@NotNull @Valid LoginRequest datos) {
        String correo = datos.correo().trim().toLowerCase(Locale.ROOT);

        Usuario usuario = usuarios.findByCorreoIgnoreCase(correo)
                .orElseThrow(this::credencialesInvalidas);

        // matches compara la contraseña ingresada con el hash de la cuenta.
        if (!passwordEncoder.matches(datos.password(), usuario.getPasswordHash())) {
            throw credencialesInvalidas();
        }

        return usuario;
    }

    // El mismo mensaje evita revelar si falló el correo o la contraseña.
    private ResponseStatusException credencialesInvalidas() {
        return new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Correo o contraseña incorrectos."
        );
    }
}