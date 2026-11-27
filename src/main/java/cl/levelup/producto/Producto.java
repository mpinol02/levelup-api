
package cl.levelup.producto;

import cl.levelup.categoria.Categoria;
import jakarta.persistence.*;
import java.math.BigDecimal;

// Representa los productos en la base de datos.
@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Codigo comercial unico.
    @Column(unique = true, length = 50)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    // Precio anterior utilizado para las ofertas.
    @Column(name = "precio_anterior", precision = 12, scale = 2)
    private BigDecimal precioAnterior;

    @Column(nullable = false)
    private Integer stock;

    // Limite para generar alertas de inventario.
    @Column(name = "stock_critico")
    private Integer stockCritico;

    @Column(length = 500)
    private String imagen;

    // Relacion entre producto y categoria.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    // Constructor requerido por JPA.
    protected Producto() {
    }

    // Mantiene compatibilidad con las pruebas anteriores.
    public Producto(
            String nombre,
            String descripcion,
            BigDecimal precio,
            Integer stock,
            Categoria categoria
    ) {
        this(
                null,
                nombre,
                descripcion,
                precio,
                stock,
                null,
                null,
                categoria
        );
    }

    // Constructor con los datos comerciales.
    public Producto(
            String codigo,
            String nombre,
            String descripcion,
            BigDecimal precio,
            Integer stock,
            Integer stockCritico,
            String imagen,
            Categoria categoria
    ) {
        this(
                codigo,
                nombre,
                descripcion,
                precio,
                null,
                stock,
                stockCritico,
                imagen,
                categoria
        );
    }

    // Constructor completo con precio anterior.
    public Producto(
            String codigo,
            String nombre,
            String descripcion,
            BigDecimal precio,
            BigDecimal precioAnterior,
            Integer stock,
            Integer stockCritico,
            String imagen,
            Categoria categoria
    ) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.precioAnterior = precioAnterior;
        this.stock = stock;
        this.stockCritico = stockCritico;
        this.imagen = imagen;
        this.categoria = categoria;
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public BigDecimal getPrecioAnterior() {
        return precioAnterior;
    }

    public void setPrecioAnterior(BigDecimal precioAnterior) {
        this.precioAnterior = precioAnterior;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getStockCritico() {
        return stockCritico;
    }

    public void setStockCritico(Integer stockCritico) {
        this.stockCritico = stockCritico;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }
}
