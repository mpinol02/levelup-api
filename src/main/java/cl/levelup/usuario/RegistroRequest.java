package cl.levelup.usuario;

import java.time.LocalDate;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

// Datos que acepta el registro público. El servidor asigna el rol Cliente.
public record RegistroRequest(
    @NotBlank @Size(min = 7, max = 9)
    String run,

    @NotBlank @Size(max = 50)
    String nombre,

    @NotBlank @Size(max = 100)
    String apellidos,

    @NotBlank @Email @Size(max = 100)
    String correo,

    @NotNull @Past
    LocalDate fechaNacimiento,

    @NotBlank
    String region,

    @NotBlank
    String comuna,

    @NotBlank @Size(max = 300)
    String direccion,

    @NotBlank @Size(min = 4, max = 10)
    String password
) {}