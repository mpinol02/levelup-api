package cl.levelup.usuario;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;

import jakarta.persistence.EntityManager;
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
public class UsuarioService {

    private static final Set<String> DOMINIOS_PERMITIDOS =
            Set.of("duoc.cl", "profesor.duoc.cl", "gmail.com");

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    public UsuarioService(UsuarioRepository usuarios,
                          PasswordEncoder passwordEncoder,
                          EntityManager entityManager) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.entityManager = entityManager;
    }

    // Registra una cuenta nueva con rol Cliente.
    @Transactional
    public Usuario registrar(@NotNull @Valid RegistroRequest datos) {
        String run = datos.run().trim().toUpperCase(Locale.ROOT);
        String correo = datos.correo().trim().toLowerCase(Locale.ROOT);

        if (!esRunValido(run)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Ingresa un RUN válido, sin puntos ni guion.");
        }

        if (datos.fechaNacimiento().isAfter(LocalDate.now().minusYears(18))) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Debes tener al menos 18 años para registrarte.");
        }

        String dominio = correo.substring(correo.lastIndexOf('@') + 1);
        if (!DOMINIOS_PERMITIDOS.contains(dominio)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Solo se permiten correos Duoc o Gmail.");
        }

        if (usuarios.existsById(run) || usuarios.existsByCorreoIgnoreCase(correo)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Ya existe un usuario con ese RUN o correo.");
        }

        Usuario nuevo = new Usuario();
        nuevo.setRun(run);
        nuevo.setNombre(datos.nombre().trim());
        nuevo.setApellidos(datos.apellidos().trim());
        nuevo.setCorreo(correo);
        nuevo.setFechaNacimiento(datos.fechaNacimiento());
        nuevo.setRegion(datos.region().trim());
        nuevo.setComuna(datos.comuna().trim());
        nuevo.setDireccion(datos.direccion().trim());
        nuevo.setRol(Rol.Cliente);
        nuevo.setPasswordHash(passwordEncoder.encode(datos.password()));

        // El RUN lo ingresa el cliente: persist inserta y nunca actualiza otra cuenta.
        entityManager.persist(nuevo);
        return nuevo;
    }

    // Calcula el dígito verificador y lo compara con el último carácter del RUN.
    private boolean esRunValido(String run) {
        if (!run.matches("[0-9]{6,8}[0-9K]")) {
            return false;
        }

        String cuerpo = run.substring(0, run.length() - 1);
        if (Integer.parseInt(cuerpo) == 0) {
            return false;
        }

        int suma = 0;
        int factor = 2;

        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += (cuerpo.charAt(i) - '0') * factor;
            factor = (factor == 7) ? 2 : factor + 1;
        }

        int resultado = 11 - (suma % 11);
        char esperado = switch (resultado) {
            case 11 -> '0';
            case 10 -> 'K';
            default -> (char) ('0' + resultado);
        };

        return run.charAt(run.length() - 1) == esperado;
    }
}