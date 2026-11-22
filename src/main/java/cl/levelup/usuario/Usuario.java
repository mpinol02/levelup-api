package cl.levelup.usuario;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

// Representa una cuenta de la tienda en la tabla usuarios.
@Entity
@Table(name = "usuarios")
public class Usuario {

    // El RUN identifica al usuario y se guarda sin puntos ni guion.
    @Id
    @Column(length = 9)
    private String run;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellidos;

    @Column(nullable = false, unique = true, length = 100)
    private String correo;

    @Column(nullable = false)
    private LocalDate fechaNacimiento;

    @Column(nullable = false)
    private String region;

    @Column(nullable = false)
    private String comuna;

    @Column(nullable = false, length = 300)
    private String direccion;

    // Solo se guarda el hash. Este campo tampoco se envía en el JSON.
    @JsonIgnore
    @Column(nullable = false)
    private String passwordHash;

    // Guarda el nombre del rol en la base de datos, por ejemplo "Cliente".
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol = Rol.Cliente;

    // JPA necesita un constructor sin argumentos para leer los registros.
    protected Usuario() {
    }

    public String getRun() { return run; }
    public String getNombre() { return nombre; }
    public String getApellidos() { return apellidos; }
    public String getCorreo() { return correo; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public String getRegion() { return region; }
    public String getComuna() { return comuna; }
    public String getDireccion() { return direccion; }
    public String getPasswordHash() { return passwordHash; }
    public Rol getRol() { return rol; }

    public void setRun(String run) { this.run = run; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public void setCorreo(String correo) { this.correo = correo; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }
    public void setRegion(String region) { this.region = region; }
    public void setComuna(String comuna) { this.comuna = comuna; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    public void setRol(Rol rol) { this.rol = rol; }
}