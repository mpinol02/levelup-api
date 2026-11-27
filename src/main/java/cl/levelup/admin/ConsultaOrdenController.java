package cl.levelup.admin;

import cl.levelup.compra.CompraResponse;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/ordenes")
public class ConsultaOrdenController {

    private final ConsultaOrdenService consultaOrdenService;

    public ConsultaOrdenController(
            ConsultaOrdenService consultaOrdenService
    ) {
        this.consultaOrdenService = consultaOrdenService;
    }

    // Administrador y Vendedor pueden consultar las ordenes.
    @GetMapping
    @PreAuthorize("hasAnyRole('Administrador', 'Vendedor')")
    public List<ConsultaOrdenService.ResumenOrden> listar() {

        return consultaOrdenService.listar();
    }

    // Ambos roles pueden consultar el detalle de una orden.
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('Administrador', 'Vendedor')")
    public CompraResponse buscarPorId(
            @PathVariable("id") Long id
    ) {
        return consultaOrdenService.buscarPorId(id);
    }

    // Solo el Administrador consulta el historial por usuario.
    @GetMapping("/usuario/{run}")
    @PreAuthorize("hasRole('Administrador')")
    public List<ConsultaOrdenService.ResumenOrden> listarPorUsuario(
            @PathVariable("run") String run
    ) {
        return consultaOrdenService.listarPorUsuario(run);
    }
}
