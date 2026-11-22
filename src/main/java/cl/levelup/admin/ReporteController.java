package cl.levelup.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    // Devuelve las estadísticas de usuarios registrados.
    @GetMapping("/usuarios")
    public ResponseEntity<ReporteResponse> reporteUsuarios() {

        ReporteResponse respuesta =
                reporteService.obtenerReporteUsuarios();

        return ResponseEntity.ok(respuesta);
    }
}
