package cl.levelup.compra;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

// Datos que envía la tienda al solicitar una compra simulada.
public record CompraRequest(

        @NotBlank(message = "El nombre del cliente es obligatorio.")
        @Size(max = 150, message = "El nombre admite hasta 150 caracteres.")
        String nombre,

        @NotBlank(message = "El correo es obligatorio.")
        @Email(message = "Ingresa un correo válido.")
        @Size(max = 100, message = "El correo admite hasta 100 caracteres.")
        String correo,

        @NotBlank(message = "Selecciona una modalidad de entrega.")
        @Pattern(
                regexp = "domicilio|retiro",
                message = "La entrega debe ser domicilio o retiro."
        )
        String entrega,

        // En retiro puede omitirse. El servicio la exigirá para domicilio.
        @Size(max = 300, message = "La dirección admite hasta 300 caracteres.")
        String direccion,

        // Corresponde a los dos botones de la simulación académica.
        @NotBlank(message = "Selecciona el resultado del pago simulado.")
        @Pattern(
                regexp = "Pagada|Pago rechazado",
                message = "El resultado del pago simulado no es válido."
        )
        String estado,

        @NotEmpty(message = "La compra debe incluir al menos un producto.")
        List<@NotNull @Valid Item> productos

) {

    // Por cada producto, React envía solo su identificador y la cantidad.
    public record Item(

            @NotNull(message = "El identificador del producto es obligatorio.")
            @Positive(message = "El identificador del producto debe ser positivo.")
            Long id,

            @NotNull(message = "La cantidad es obligatoria.")
            @Positive(message = "La cantidad debe ser mayor que cero.")
            Integer cantidad

    ) {}
}