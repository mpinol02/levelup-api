package cl.levelup.compra;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

// Guarda una línea del comprobante: qué producto se compró, a qué precio y cuántos.
@Embeddable
public class DetalleOrden {

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(name = "producto_nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false)
    private int cantidad;

    // JPA usa este constructor para leer el detalle desde la base de datos.
    protected DetalleOrden() {
    }

    public DetalleOrden(Long productoId, String nombre, BigDecimal precio, int cantidad) {
        this.productoId = productoId;
        this.nombre = nombre;
        this.precio = precio;
        this.cantidad = cantidad;
    }

    public Long getProductoId() { return productoId; }
    public String getNombre() { return nombre; }
    public BigDecimal getPrecio() { return precio; }
    public int getCantidad() { return cantidad; }

    // Calcula el subtotal usando el precio guardado en la compra.
    public BigDecimal getSubtotal() {
        return precio.multiply(BigDecimal.valueOf(cantidad));
    }
}