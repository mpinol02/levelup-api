package cl.levelup.admin;

// Respuesta con las estadisticas de contacto y soporte.
public record ReporteContactoResponse(
        long totalContactos,
        long totalPendientes,
        long totalAtendidos
) {}
