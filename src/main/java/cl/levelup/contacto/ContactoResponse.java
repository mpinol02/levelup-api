package cl.levelup.contacto;

import java.time.LocalDateTime;

public record ContactoResponse(

        Long id,
        String nombre,
        String correo,
        String comentario,
        LocalDateTime fechaEnvio,
        boolean atendido

) {

    // Convierte una entidad en una respuesta de la API.
    public static ContactoResponse desdeContacto(
            Contacto contacto
    ) {
        return new ContactoResponse(
                contacto.getId(),
                contacto.getNombre(),
                contacto.getCorreo(),
                contacto.getComentario(),
                contacto.getFechaEnvio(),
                contacto.isAtendido()
        );
    }
}
