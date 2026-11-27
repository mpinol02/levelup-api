
package cl.levelup.compra;

import cl.levelup.carrito.CarritoItem;
import cl.levelup.usuario.Usuario;
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

    private static final BigDecimal COSTO_DOMICILIO =
            new BigDecimal("3000");

    private final EntityManager entityManager;
    private final OrdenRepository ordenRepository;

    public CompraService(
            EntityManager entityManager,
            OrdenRepository ordenRepository
    ) {
        this.entityManager = entityManager;
        this.ordenRepository = ordenRepository;
    }

    // Guarda la orden, actualiza el stock y limpia el carrito
    // dentro de una misma transacción.
    @Transactional
    public CompraResponse procesar(
            @NotNull @Valid CompraRequest datos,
            String runAutenticado,
            boolean tieneBeneficioDuoc
    ) {

        // Verifica que exista una sesión autenticada.
        if (runAutenticado == null || runAutenticado.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Inicia sesión para comprar."
            );
        }

        // Bloquea al usuario para impedir modificaciones
        // simultáneas de su carrito durante la compra.
        Usuario usuario = entityManager.find(
                Usuario.class,
                runAutenticado,
                LockModeType.PESSIMISTIC_WRITE
        );

        if (usuario == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "La cuenta de la sesión ya no existe."
            );
        }

        boolean esDomicilio =
                "domicilio".equals(datos.entrega());

        boolean pagoAprobado =
                "Pagada".equals(datos.estado());

        // Valida la dirección cuando corresponde a despacho.
        if (esDomicilio && (
                datos.direccion() == null
                || datos.direccion().isBlank()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ingresa una dirección para el despacho."
            );
        }

        // Ordena los productos por ID y evita duplicados.
        var cantidades = new TreeMap<Long, Integer>();

        for (CompraRequest.Item item : datos.productos()) {

            if (cantidades.putIfAbsent(
                    item.id(),
                    item.cantidad()
            ) != null) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Hay un producto repetido en el carrito."
                );
            }
        }

        // Recupera el carrito real desde la base de datos.
        var cantidadesCarrito = new TreeMap<Long, Integer>();

        List<CarritoItem> carrito = entityManager.createQuery(
                "SELECT c FROM CarritoItem c "
                        + "WHERE c.usuario.run = :run",
                CarritoItem.class
        )
        .setParameter("run", runAutenticado)
        .getResultList();

        for (CarritoItem item : carrito) {
            cantidadesCarrito.put(
                    item.getProducto().getId(),
                    item.getCantidad()
            );
        }

        // Impide realizar compras con un carrito vacío.
        if (cantidadesCarrito.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Tu carrito está vacío."
            );
        }

        // Verifica que React envíe los mismos productos
        // y cantidades almacenados en el backend.
        if (!cantidadesCarrito.equals(cantidades)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El carrito cambió. Revisa los productos antes de comprar."
            );
        }

        List<DetalleOrden> detalles = new ArrayList<>();

        BigDecimal subtotal = BigDecimal.ZERO;

        for (var entrada : cantidadesCarrito.entrySet()) {

            Long id = entrada.getKey();
            int cantidad = entrada.getValue();

            // Bloquea el producto para evitar que dos compras
            // simultáneas descuenten las mismas unidades.
            Producto producto = entityManager.find(
                    Producto.class,
                    id,
                    LockModeType.PESSIMISTIC_WRITE
            );

            if (producto == null) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Ya no existe el producto con ID "
                                + id + "."
                );
            }

            // Valida nuevamente el stock antes de comprar.
            if (producto.getStock() < cantidad) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Stock insuficiente para "
                                + producto.getNombre()
                                + ". Disponible: "
                                + producto.getStock()
                                + "."
                );
            }

            // Utiliza el precio real de la base de datos.
            DetalleOrden detalle = new DetalleOrden(
                    producto.getId(),
                    producto.getNombre(),
                    producto.getPrecio(),
                    cantidad
            );

            detalles.add(detalle);

            subtotal = subtotal.add(
                    detalle.getSubtotal()
            );

            // Solo descuenta stock si el pago fue aprobado.
            if (pagoAprobado) {
                producto.setStock(
                        producto.getStock() - cantidad
                );
            }
        }

        // Aplica el beneficio Duoc según la cuenta autenticada.
        int porcentaje = tieneBeneficioDuoc ? 20 : 0;

        BigDecimal descuento = subtotal
                .multiply(BigDecimal.valueOf(porcentaje))
                .movePointLeft(2)
                .setScale(0, RoundingMode.HALF_UP);

        // Calcula el costo según la modalidad de entrega.
        BigDecimal envio = esDomicilio
                ? COSTO_DOMICILIO
                : BigDecimal.ZERO;

        // Construye la orden con los datos de la compra.
        Orden orden = new Orden(
                runAutenticado,
                datos.nombre().trim(),
                datos.correo().trim(),
                esDomicilio
                        ? datos.direccion().trim()
                        : "Retiro en tienda",
                esDomicilio
                        ? "Despacho a domicilio"
                        : "Retiro en tienda",
                datos.estado(),
                detalles,
                porcentaje,
                descuento,
                envio
        );

        // Guarda la orden en la base de datos.
        Orden ordenGuardada = ordenRepository.save(orden);

        // Vacía el carrito únicamente si el pago fue aprobado.
        // Si ocurre un error, la transacción revierte los cambios.
        if (pagoAprobado) {

            for (CarritoItem item : carrito) {
                entityManager.remove(item);
            }
        }

        return prepararRespuesta(ordenGuardada);
    }

    // Convierte la orden guardada al formato del comprobante.
    private CompraResponse prepararRespuesta(Orden orden) {

        var cliente = new CompraResponse.Cliente(
                orden.getClienteRun(),
                orden.getClienteNombre(),
                orden.getClienteCorreo(),
                orden.getDireccion()
        );

        var productos = orden.getProductos()
                .stream()
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
