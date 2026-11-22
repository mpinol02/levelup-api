
package cl.levelup.admin;

import cl.levelup.usuario.Rol;

// Datos públicos para la administración de usuarios.
public record AdminUsuarioResponse(
        String run,
        String nombre,
        String apellidos,
        String correo,
        Rol rol
) {}
