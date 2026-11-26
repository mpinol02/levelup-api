package cl.levelup.admin;

import cl.levelup.compra.CompraResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/ordenes")
public class ConsultaOrdenController {

    private final ConsultaOrdenService consultaOrdenService;

    public ConsultaOrdenController(ConsultaOrdenService consultaOrdenService) {
        this.consultaOrdenService = consultaOrdenService;
    }

    // Muestra el resumen de las órdenes en el panel de administración.
    @GetMapping
    public List<ConsultaOrdenService.ResumenOrden> listar() {
        return consultaOrdenService.listar();
    }

    // Recupera el comprobante completo de la orden seleccionada.
    @GetMapping("/{id}")
    public CompraResponse buscarPorId(@PathVariable("id") Long id) {
        return consultaOrdenService.buscarPorId(id);
    }
}