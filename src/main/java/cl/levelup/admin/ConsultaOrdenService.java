package cl.levelup.admin;

import cl.levelup.compra.CompraResponse;
import cl.levelup.compra.Orden;
import cl.levelup.compra.OrdenRepository;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ConsultaOrdenService {

    private final OrdenRepository ordenRepository;

    public ConsultaOrdenService(OrdenRepository ordenRepository) {
        this.ordenRepository = ordenRepository;
    }

    // Muestra las órdenes más recientes primero, tanto pagadas como rechazadas.
    public List<ResumenOrden> listar() {
        Sort recientesPrimero = Sort.by(
                Sort.Order.desc("fecha"),
                Sort.Order.desc("id")
        );

        return ordenRepository.findAll(recientesPrimero).stream()
                .map(orden -> new ResumenOrden(
                        orden.getId(),
                        orden.getFecha(),
                        new ClienteResumen(orden.getClienteNombre()),
                        orden.getEstado(),
                        orden.getTotal()
                ))
                .toList();
    }

    // Busca una orden por su número y devuelve su comprobante completo.
    public CompraResponse buscarPorId(Long id) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "El número de orden debe ser positivo."
            );
        }

        Orden orden = ordenRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "La orden no existe."
                ));

        var cliente = new CompraResponse.Cliente(
                orden.getClienteRun(),
                orden.getClienteNombre(),
                orden.getClienteCorreo(),
                orden.getDireccion()
        );

        // Usa el precio guardado en la compra, aunque el catálogo haya cambiado.
        var productos = orden.getProductos().stream()
                .map(item -> new CompraResponse.Item(
                        item.getProductoId(),
                        item.getNombre(),
                        item.getPrecio(),
                        item.getCantidad(),
                        item.getSubtotal()
                ))
                .toList();

        return new CompraResponse(
                orden.getId(),
                orden.getFecha(),
                cliente,
                orden.getEntrega(),
                orden.getEstado(),
                productos,
                orden.getSubtotal(),
                orden.getPorcentajeDescuento(),
                orden.getDescuento(),
                orden.getCostoEnvio(),
                orden.getTotal()
        );
    }

    // La tabla del panel necesita un resumen; los productos se leen al abrir el detalle.
    public record ResumenOrden(
            Long id,
            Instant fecha,
            ClienteResumen cliente,
            String estado,
            BigDecimal total
    ) {}

    public record ClienteResumen(String nombre) {}
}