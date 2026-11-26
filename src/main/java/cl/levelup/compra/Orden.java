package cl.levelup.compra;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

// Guarda el comprobante de una compra pagada o rechazada.
@Entity
@Table(name = "ordenes")
public class Orden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant fecha;

    @Column(name = "cliente_run", nullable = false, length = 9)
    private String clienteRun;

    @Column(name = "cliente_nombre", nullable = false, length = 150)
    private String clienteNombre;

    @Column(name = "cliente_correo", nullable = false, length = 100)
    private String clienteCorreo;

    @Column(nullable = false, length = 300)
    private String direccion;

    @Column(nullable = false, length = 40)
    private String entrega;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "porcentaje_descuento", nullable = false)
    private int porcentajeDescuento;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal descuento;

    @Column(name = "costo_envio", nullable = false, precision = 19, scale = 2)
    private BigDecimal costoEnvio;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal total;

    // Cada producto del comprobante se guarda en una fila ligada a la orden.
    @ElementCollection
    @CollectionTable(
            name = "orden_detalles",
            joinColumns = @JoinColumn(name = "orden_id")
    )
    @OrderColumn(name = "posicion")
    private List<DetalleOrden> productos = new ArrayList<>();

    // JPA necesita este constructor para leer una orden guardada.
    protected Orden() {
    }

    // Crea la orden con los datos del cliente y los importes del backend.
    public Orden(
            String clienteRun,
            String clienteNombre,
            String clienteCorreo,
            String direccion,
            String entrega,
            String estado,
            List<DetalleOrden> productos,
            int porcentajeDescuento,
            BigDecimal descuento,
            BigDecimal costoEnvio
    ) {
        this.fecha = Instant.now();
        this.clienteRun = clienteRun;
        this.clienteNombre = clienteNombre;
        this.clienteCorreo = clienteCorreo;
        this.direccion = direccion;
        this.entrega = entrega;
        this.estado = estado;
        this.productos = new ArrayList<>(productos);
        this.porcentajeDescuento = porcentajeDescuento;
        this.descuento = descuento;
        this.costoEnvio = costoEnvio;

        // Suma el precio por cantidad de cada producto del comprobante.
        this.subtotal = this.productos.stream()
                .map(DetalleOrden::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Al subtotal se le resta el descuento y se le suma el envío.
        this.total = subtotal.subtract(descuento).add(costoEnvio);
    }

    public Long getId() { return id; }
    public Instant getFecha() { return fecha; }
    public String getClienteRun() { return clienteRun; }
    public String getClienteNombre() { return clienteNombre; }
    public String getClienteCorreo() { return clienteCorreo; }
    public String getDireccion() { return direccion; }
    public String getEntrega() { return entrega; }
    public String getEstado() { return estado; }
    public List<DetalleOrden> getProductos() { return List.copyOf(productos); }
    public BigDecimal getSubtotal() { return subtotal; }
    public int getPorcentajeDescuento() { return porcentajeDescuento; }
    public BigDecimal getDescuento() { return descuento; }
    public BigDecimal getCostoEnvio() { return costoEnvio; }
    public BigDecimal getTotal() { return total; }
}