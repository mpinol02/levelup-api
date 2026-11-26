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
@PreAuthorize("hasRole('Administrador')")
public class ConsultaOrdenController {

    private final ConsultaOrdenService consultaOrdenService;

    public ConsultaOrdenController(
            ConsultaOrdenService consultaOrdenService
    ) {
        this.consultaOrdenService = consultaOrdenService;
    }

    // Lista todas las ordenes registradas.
    @GetMapping
    public List<ConsultaOrdenService.ResumenOrden> listar() {

        return consultaOrdenService.listar();
    }

    // Recupera el comprobante completo de una orden.
    @GetMapping("/{id}")
    public CompraResponse buscarPorId(
            @PathVariable("id") Long id
    ) {
        return consultaOrdenService.buscarPorId(id);
    }

    // Consulta el historial de compras pagadas de un usuario.
    @GetMapping("/usuario/{run}")
    public List<ConsultaOrdenService.ResumenOrden> listarPorUsuario(
            @PathVariable("run") String run
    ) {
        return consultaOrdenService.listarPorUsuario(run);
    }
}
