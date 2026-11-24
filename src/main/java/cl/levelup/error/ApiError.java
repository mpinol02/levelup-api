package cl.levelup.error;

import java.time.OffsetDateTime;
import java.util.Map;

// Define el formato de las respuestas de error de la API.
public record ApiError(

        // Fecha y hora en que ocurrio el error.
        OffsetDateTime timestamp,

        // Codigo HTTP del error.
        int status,

        // Nombre del tipo de error.
        String error,

        // Explicacion del problema.
        String mensaje,

        // Endpoint donde ocurrio el error.
        String path,

        // Errores especificos de los campos enviados.
        Map<String, String> erroresCampo

) {

    // Crea una respuesta de error sin detalles de campos.
    public static ApiError crear(
            int status,
            String error,
            String mensaje,
            String path
    ) {
        return new ApiError(
                OffsetDateTime.now(),
                status,
                error,
                mensaje,
                path,
                Map.of()
        );
    }

    // Crea una respuesta con los campos que no pasaron la validacion.
    public static ApiError conCampos(
            int status,
            String error,
            String mensaje,
            String path,
            Map<String, String> erroresCampo
    ) {
        return new ApiError(
                OffsetDateTime.now(),
                status,
                error,
                mensaje,
                path,
                Map.copyOf(erroresCampo)
        );
    }
}
