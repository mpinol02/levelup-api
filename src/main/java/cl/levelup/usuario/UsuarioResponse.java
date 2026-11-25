package cl.levelup.usuario;

import java.time.LocalDate;

// Representa los datos publicos del perfil de usuario.
// No incluye la contrasena ni su hash.
public record UsuarioResponse(
        String run,
        String nombre,
        String apellidos,
        String correo,
        LocalDate fechaNacimiento,
        String region,
        String comuna,
        String direccion,
        Rol rol
) {

    // Convierte una entidad Usuario en una respuesta segura.
    public static UsuarioResponse desdeUsuario(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getRun(),
                usuario.getNombre(),
                usuario.getApellidos(),
                usuario.getCorreo(),
                usuario.getFechaNacimiento(),
                usuario.getRegion(),
                usuario.getComuna(),
                usuario.getDireccion(),
                usuario.getRol()
        );
    }
}
