
package cl.levelup.carrito;

import cl.levelup.producto.Producto;
import cl.levelup.usuario.Usuario;

import jakarta.persistence.*;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

// Representa un producto agregado al carrito de un usuario.
@Entity
@Table(
        name = "carrito_items",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_carrito_usuario_producto",
                        columnNames = {
                                "usuario_run",
                                "producto_id"
                        }
                )
        }
)
public class CarritoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Usuario propietario del carrito.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_run", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Usuario usuario;

    // Producto agregado al carrito.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Producto producto;

    // Cantidad solicitada por el usuario.
    @Column(nullable = false)
    private Integer cantidad;

    // Constructor requerido por JPA.
    protected CarritoItem() {
    }

    public CarritoItem(
            Usuario usuario,
            Producto producto,
            Integer cantidad
    ) {
        this.usuario = usuario;
        this.producto = producto;
        this.cantidad = cantidad;
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Producto getProducto() {
        return producto;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
}
