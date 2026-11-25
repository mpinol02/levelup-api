package cl.levelup.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/reportes")
@PreAuthorize("hasRole('Administrador')")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    // Devuelve las estadisticas de usuarios registrados.
    @GetMapping("/usuarios")
    public ResponseEntity<ReporteResponse> reporteUsuarios() {

        ReporteResponse respuesta =
                reporteService.obtenerReporteUsuarios();

        return ResponseEntity.ok(respuesta);
    }

    // Devuelve las estadisticas de contacto y soporte.
    @GetMapping("/contactos")
    public ResponseEntity<ReporteContactoResponse> reporteContactos() {

        ReporteContactoResponse respuesta =
                reporteService.obtenerReporteContactos();

        return ResponseEntity.ok(respuesta);
    }
}
