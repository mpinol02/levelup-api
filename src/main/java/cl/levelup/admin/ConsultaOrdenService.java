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

    public ConsultaOrdenService(
            OrdenRepository ordenRepository
    ) {
        this.ordenRepository = ordenRepository;
    }

    // Lista las ordenes pagadas y rechazadas.
    public List<ResumenOrden> listar() {

        Sort recientesPrimero = Sort.by(
                Sort.Order.desc("fecha"),
                Sort.Order.desc("id")
        );

        return ordenRepository.findAll(recientesPrimero)
                .stream()
                .map(this::crearResumen)
                .toList();
    }

    // Consulta las compras pagadas de un usuario.
    public List<ResumenOrden> listarPorUsuario(String run) {

        return ordenRepository
                .findByClienteRunAndEstadoOrderByFechaDescIdDesc(
                        run,
                        "Pagada"
                )
                .stream()
                .map(this::crearResumen)
                .toList();
    }

    // Convierte una orden en un resumen para las tablas.
    private ResumenOrden crearResumen(Orden orden) {

        return new ResumenOrden(
                orden.getId(),
                orden.getFecha(),
                new ClienteResumen(
                        orden.getClienteNombre()
                ),
                orden.getEstado(),
                orden.getTotal()
        );
    }

    // Recupera el comprobante completo de una orden.
    public CompraResponse buscarPorId(Long id) {

        if (id == null || id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El numero de orden debe ser positivo."
            );
        }

        Orden orden = ordenRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "La orden no existe."
                        )
                );

        var cliente = new CompraResponse.Cliente(
                orden.getClienteRun(),
                orden.getClienteNombre(),
                orden.getClienteCorreo(),
                orden.getDireccion()
        );

        // Utiliza los precios guardados al realizar la compra.
        var productos = orden.getProductos()
                .stream()
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

    // Resumen de una orden para las vistas administrativas.
    public record ResumenOrden(
            Long id,
            Instant fecha,
            ClienteResumen cliente,
            String estado,
            BigDecimal total
    ) {}

    public record ClienteResumen(
            String nombre
    ) {}
}
