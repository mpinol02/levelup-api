package cl.levelup.auth;

import cl.levelup.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

// Representa un token de renovacion asociado a un usuario.
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    // Identificador interno generado por la base de datos.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relaciona el token con el usuario propietario.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_run", nullable = false)
    private Usuario usuario;

    // Guarda solo el hash del token, nunca su valor original.
    @Column(nullable = false, unique = true, length = 64)
    private String tokenHash;

    // Registra cuando fue creado el token.
    @Column(nullable = false)
    private Instant creadoEn;

    // Indica hasta cuando se puede renovar la sesion.
    @Column(nullable = false)
    private Instant venceEn;

    // Permite invalidar un token antes de su vencimiento.
    @Column
    private Instant revocadoEn;

    // Constructor requerido por JPA.
    protected RefreshToken() {
    }

    // Crea un registro de renovacion para un usuario.
    public RefreshToken(
            Usuario usuario,
            String tokenHash,
            Instant creadoEn,
            Instant venceEn
    ) {
        this.usuario = usuario;
        this.tokenHash = tokenHash;
        this.creadoEn = creadoEn;
        this.venceEn = venceEn;
    }

    // Comprueba si el token sigue vigente y no fue revocado.
    public boolean estaVigente(Instant ahora) {
        return revocadoEn == null && ahora.isBefore(venceEn);
    }

    // Invalida el token para evitar que vuelva a utilizarse.
    public void revocar(Instant ahora) {
        if (revocadoEn == null) {
            revocadoEn = ahora;
        }
    }

    // Permite consultar los datos del registro.
    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public Instant getVenceEn() {
        return venceEn;
    }

    public Instant getRevocadoEn() {
        return revocadoEn;
    }
}
