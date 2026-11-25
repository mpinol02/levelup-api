package cl.levelup.contacto;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// Representa una consulta enviada desde el formulario de contacto.
@Entity
@Table(name = "contactos")
public class Contacto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 100)
    private String correo;

    @Column(nullable = false, length = 500)
    private String comentario;

    @Column(nullable = false)
    private LocalDateTime fechaEnvio;

    @Column(nullable = false)
    private boolean atendido;

    // Constructor requerido por JPA.
    protected Contacto() {
    }

    // Crea una consulta nueva con estado pendiente.
    public Contacto(
            String nombre,
            String correo,
            String comentario
    ) {
        this.nombre = nombre;
        this.correo = correo;
        this.comentario = comentario;
        this.fechaEnvio = LocalDateTime.now();
        this.atendido = false;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public String getComentario() {
        return comentario;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public boolean isAtendido() {
        return atendido;
    }

    // Permite cambiar el estado de la consulta.
    public void setAtendido(boolean atendido) {
        this.atendido = atendido;
    }
}
