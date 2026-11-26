package cl.levelup.compra;

import cl.levelup.producto.Producto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

@Service
@Validated
public class CompraService {

    private static final BigDecimal COSTO_DOMICILIO = new BigDecimal("3000");

    private final EntityManager entityManager;
    private final OrdenRepository ordenRepository;

    public CompraService(EntityManager entityManager, OrdenRepository ordenRepository) {
        this.entityManager = entityManager;
        this.ordenRepository = ordenRepository;
    }

    // Guarda la orden y actualiza el stock en una sola operación.
    @Transactional
    public CompraResponse procesar(
            @NotNull @Valid CompraRequest datos,
            String runAutenticado,
            boolean tieneBeneficioDuoc
    ) {
        // El controlador debe obtener el RUN y el beneficio desde el backend.
        if (runAutenticado == null || runAutenticado.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Inicia sesión para comprar."
            );
        }

        boolean esDomicilio = "domicilio".equals(datos.entrega());
        boolean pagoAprobado = "Pagada".equals(datos.estado());

        if (esDomicilio && (datos.direccion() == null || datos.direccion().isBlank())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Ingresa una dirección para el despacho."
            );
        }

        // Un mismo producto debe llegar en una sola línea con su cantidad total.
        // Ordenar por ID también permite bloquear los productos siempre en el mismo orden.
        var cantidades = new TreeMap<Long, Integer>();

        for (CompraRequest.Item item : datos.productos()) {
            if (cantidades.putIfAbsent(item.id(), item.cantidad()) != null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Hay un producto repetido en el carrito."
                );
            }
        }

        List<DetalleOrden> detalles = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (var entrada : cantidades.entrySet()) {
            Long id = entrada.getKey();
            int cantidad = entrada.getValue();

            // Dos compras simultáneas no pueden descontar las mismas unidades.
            Producto producto = entityManager.find(
                    Producto.class, id, LockModeType.PESSIMISTIC_WRITE
            );

            if (producto == null) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ya no existe el producto con ID " + id + "."
                );
            }

            if (producto.getStock() < cantidad) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Stock insuficiente para " + producto.getNombre()
                                + ". Disponible: " + producto.getStock() + "."
                );
            }

            // El precio se toma de la base de datos al momento de la compra.
            DetalleOrden detalle = new DetalleOrden(
                    producto.getId(), producto.getNombre(), producto.getPrecio(), cantidad
            );

            detalles.add(detalle);
            subtotal = subtotal.add(detalle.getSubtotal());

            if (pagoAprobado) {
                producto.setStock(producto.getStock() - cantidad);
            }
        }

        int porcentaje = tieneBeneficioDuoc ? 20 : 0;

        // El descuento se redondea al peso, igual que en la tienda.
        BigDecimal descuento = subtotal
                .multiply(BigDecimal.valueOf(porcentaje))
                .movePointLeft(2)
                .setScale(0, RoundingMode.HALF_UP);

        BigDecimal envio = esDomicilio ? COSTO_DOMICILIO : BigDecimal.ZERO;

        Orden orden = new Orden(
                runAutenticado,
                datos.nombre().trim(),
                datos.correo().trim(),
                esDomicilio ? datos.direccion().trim() : "Retiro en tienda",
                esDomicilio ? "Despacho a domicilio" : "Retiro en tienda",
                datos.estado(),
                detalles,
                porcentaje,
                descuento,
                envio
        );

        return prepararRespuesta(ordenRepository.save(orden));
    }

    // Convierte la orden guardada al formato del comprobante que recibe React.
    private CompraResponse prepararRespuesta(Orden orden) {
        var cliente = new CompraResponse.Cliente(
                orden.getClienteRun(),
                orden.getClienteNombre(),
                orden.getClienteCorreo(),
                orden.getDireccion()
        );

        var productos = orden.getProductos().stream()
                .map(detalle -> new CompraResponse.Item(
                        detalle.getProductoId(),
                        detalle.getNombre(),
                        detalle.getPrecio(),
                        detalle.getCantidad(),
                        detalle.getSubtotal()
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
}