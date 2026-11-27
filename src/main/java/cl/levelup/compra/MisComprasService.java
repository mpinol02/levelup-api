
package cl.levelup.compra;

import cl.levelup.admin.ConsultaOrdenService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class MisComprasService {

    private final OrdenRepository ordenRepository;
    private final ConsultaOrdenService consultaOrdenService;

    public MisComprasService(
            OrdenRepository ordenRepository,
            ConsultaOrdenService consultaOrdenService
    ) {
        this.ordenRepository = ordenRepository;
        this.consultaOrdenService = consultaOrdenService;
    }

    // Lista únicamente las compras de la cuenta autenticada.
    public List<ResumenCompra> listar(String run) {

        return ordenRepository
                .findByClienteRunOrderByFechaDescIdDesc(run)
                .stream()
                .map(orden -> new ResumenCompra(
                        orden.getId(),
                        orden.getFecha(),
                        orden.getEstado(),
                        orden.getTotal()
                ))
                .toList();
    }

    // Recupera un comprobante verificando primero su propietario.
    public CompraResponse buscarPorId(
            Long id,
            String run
    ) {

        if (id == null || id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El número de orden debe ser positivo."
            );
        }

        Orden orden = ordenRepository
                .findByIdAndClienteRun(id, run)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "La orden no existe."
                        )
                );

        // Reutiliza la generación del comprobante existente.
        return consultaOrdenService.buscarPorId(
                orden.getId()
        );
    }

    // Información resumida para el historial personal.
    public record ResumenCompra(
            Long id,
            Instant fecha,
            String estado,
            BigDecimal total
    ) {
    }
}
