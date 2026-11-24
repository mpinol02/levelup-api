package cl.levelup.producto;

import cl.levelup.categoria.Categoria;
import jakarta.persistence.*;
import java.math.BigDecimal;

// Representa los productos de Level-Up Gamer en la base de datos.
@Entity
@Table(name = "productos")
public class Producto {

    // Identificador unico generado automaticamente.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nombre del producto.
    @Column(nullable = false, length = 150)
    private String nombre;

    // Descripcion del producto.
    @Column(length = 1000)
    private String descripcion;

    // Precio del producto, con dos decimales.
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    // Cantidad disponible en inventario.
    @Column(nullable = false)
    private Integer stock;

    // Cada producto pertenece a una categoria.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    // Constructor requerido por JPA.
    protected Producto() {
    }

    // Permite crear productos con sus datos principales.
    public Producto(
            String nombre,
            String descripcion,
            BigDecimal precio,
            Integer stock,
            Categoria categoria
    ) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
        this.categoria = categoria;
    }

    // Obtiene el identificador del producto.
    public Long getId() {
        return id;
    }

    // Obtiene el nombre del producto.
    public String getNombre() {
        return nombre;
    }

    // Modifica el nombre del producto.
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    // Obtiene la descripcion del producto.
    public String getDescripcion() {
        return descripcion;
    }

    // Modifica la descripcion del producto.
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    // Obtiene el precio del producto.
    public BigDecimal getPrecio() {
        return precio;
    }

    // Modifica el precio del producto.
    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    // Obtiene la cantidad disponible en inventario.
    public Integer getStock() {
        return stock;
    }

    // Actualiza la cantidad disponible.
    public void setStock(Integer stock) {
        this.stock = stock;
    }

    // Obtiene la categoria asociada al producto.
    public Categoria getCategoria() {
        return categoria;
    }

    // Cambia la categoria del producto.
    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }
}
