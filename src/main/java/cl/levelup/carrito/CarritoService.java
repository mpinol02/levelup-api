
package cl.levelup.carrito;

import cl.levelup.producto.Producto;
import cl.levelup.producto.ProductoRepository;
import cl.levelup.usuario.Usuario;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final ProductoRepository productoRepository;
    private final EntityManager entityManager;

    public CarritoService(
            CarritoRepository carritoRepository,
            ProductoRepository productoRepository,
            EntityManager entityManager
    ) {
        this.carritoRepository = carritoRepository;
        this.productoRepository = productoRepository;
        this.entityManager = entityManager;
    }

    // Consulta el carrito del usuario autenticado.
    @Transactional(readOnly = true)
    public CarritoResponse consultar(String run) {
        return prepararRespuesta(run);
    }

    // Agrega un producto o incrementa su cantidad.
    @Transactional
    public CarritoResponse agregar(
            String run,
            CarritoRequest datos
    ) {
        Usuario usuario = bloquearUsuario(run);
        Producto producto = obtenerProducto(datos.productoId());

        CarritoItem item = carritoRepository
                .findByUsuario_RunAndProducto_Id(
                        run, producto.getId()
                )
                .orElse(null);

        long cantidadActual = item == null
                ? 0 : item.getCantidad();

        long nuevaCantidad =
                cantidadActual + datos.cantidad();

        validarStock(producto, nuevaCantidad);

        if (item == null) {
            item = new CarritoItem(
                    usuario,
                    producto,
                    (int) nuevaCantidad
            );
        } else {
            item.setCantidad((int) nuevaCantidad);
        }

        carritoRepository.save(item);

        return prepararRespuesta(run);
    }

    // Reemplaza la cantidad de un producto existente.
    @Transactional
    public CarritoResponse actualizar(
            String run,
            Long productoId,
            Integer cantidad
    ) {
        bloquearUsuario(run);

        CarritoItem item = carritoRepository
                .findByUsuario_RunAndProducto_Id(run, productoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "El producto no está en tu carrito."
                ));

        Producto producto = obtenerProducto(productoId);

        validarStock(producto, cantidad);

        item.setCantidad(cantidad);
        carritoRepository.save(item);

        return prepararRespuesta(run);
    }

    // Elimina un producto del carrito personal.
    @Transactional
    public CarritoResponse eliminar(
            String run,
            Long productoId
    ) {
        bloquearUsuario(run);

        carritoRepository
                .deleteByUsuario_RunAndProducto_Id(
                        run, productoId
                );

        return prepararRespuesta(run);
    }

    // Elimina todos los productos del carrito personal.
    @Transactional
    public CarritoResponse vaciar(String run) {
        bloquearUsuario(run);

        carritoRepository.deleteByUsuario_Run(run);

        return prepararRespuesta(run);
    }

    // Evita modificaciones simultáneas del mismo carrito.
    private Usuario bloquearUsuario(String run) {

        Usuario usuario = entityManager.find(
                Usuario.class,
                run,
                LockModeType.PESSIMISTIC_WRITE
        );

        if (usuario == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "La cuenta de la sesión ya no existe."
            );
        }

        return usuario;
    }

    // Comprueba que el producto exista en la base de datos.
    private Producto obtenerProducto(Long productoId) {

        return productoRepository.findById(productoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "El producto no existe."
                ));
    }

    // Impide agregar cantidades superiores al stock.
    private void validarStock(
            Producto producto,
            long cantidad
    ) {
        if (cantidad < 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La cantidad debe ser mayor que cero."
            );
        }

        if (cantidad > producto.getStock()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Stock insuficiente para "
                            + producto.getNombre()
                            + ". Disponible: "
                            + producto.getStock()
            );
        }
    }

    // Calcula los totales utilizando los precios del backend.
    private CarritoResponse prepararRespuesta(String run) {

        List<CarritoResponse.Item> productos =
                carritoRepository
                        .findByUsuario_RunOrderByIdAsc(run)
                        .stream()
                        .map(item -> {

                            Producto producto = item.getProducto();

                            BigDecimal subtotal =
                                    producto.getPrecio().multiply(
                                            BigDecimal.valueOf(
                                                    item.getCantidad()
                                            )
                                    );

                            return new CarritoResponse.Item(
                                    producto.getId(),
                                    producto.getCodigo(),
                                    producto.getNombre(),
                                    producto.getPrecio(),
                                    item.getCantidad(),
                                    producto.getStock(),
                                    subtotal
                            );
                        })
                        .toList();

        long cantidadTotal = productos.stream()
                .mapToLong(CarritoResponse.Item::cantidad)
                .sum();

        BigDecimal subtotal = productos.stream()
                .map(CarritoResponse.Item::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CarritoResponse(
                productos,
                cantidadTotal,
                subtotal
        );
    }
}
