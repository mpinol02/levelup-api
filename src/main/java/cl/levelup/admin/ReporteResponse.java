package cl.levelup.admin;

// Respuesta con las estadísticas de usuarios de Level-Up.
public record ReporteResponse(
        long totalUsuarios,
        long totalClientes,
        long totalAdministradores,
        long totalVendedores
) {}
