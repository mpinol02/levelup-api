package cl.levelup;

import java.time.LocalDate;

import cl.levelup.usuario.PerfilRequest;
import cl.levelup.usuario.RegistroRequest;
import cl.levelup.usuario.Usuario;
import cl.levelup.usuario.UsuarioRepository;
import cl.levelup.usuario.UsuarioResponse;
import cl.levelup.usuario.UsuarioService;

import jakarta.validation.ConstraintViolationException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
class PerfilTest {

    @Autowired
    private UsuarioService servicio;

    @Autowired
    private UsuarioRepository usuarios;

    // Registra un usuario ficticio para cada prueba.
    // La transaccion deshace los cambios al finalizar.
    private void crearUsuario(
            String run,
            String correo,
            String nombre
    ) {
        RegistroRequest datos = new RegistroRequest(
                run,
                nombre,
                "Prueba",
                correo,
                LocalDate.of(2000, 1, 1),
                "Metropolitana",
                "Maipu",
                "Direccion original",
                "clave123"
        );

        servicio.registrar(datos);
        usuarios.flush();
    }

    // Comprueba que el servicio devuelve el perfil correcto.
    @Test
    void consultaElPerfilDelUsuario() {
        crearUsuario(
                "123456785",
                "perfil.consulta@gmail.com",
                "Mario"
        );

        UsuarioResponse perfil =
                servicio.consultarPerfil("123456785");

        assertThat(perfil.run()).isEqualTo("123456785");
        assertThat(perfil.nombre()).isEqualTo("Mario");
        assertThat(perfil.correo())
                .isEqualTo("perfil.consulta@gmail.com");
        assertThat(perfil.direccion())
                .isEqualTo("Direccion original");
    }

    // Comprueba que el nombre, apellidos y direccion
    // se actualizan realmente en la base de datos.
    @Test
    void actualizaLosDatosPermitidosDelPerfil() {
        crearUsuario(
                "123456785",
                "perfil.edicion@gmail.com",
                "Mario"
        );

        PerfilRequest cambios = new PerfilRequest(
                "Mario Actualizado",
                "Apellido Nuevo",
                "Direccion nueva 456"
        );

        UsuarioResponse respuesta =
                servicio.actualizarPerfil(
                        "123456785",
                        cambios
                );

        usuarios.flush();

        Usuario guardado = usuarios
                .findById("123456785")
                .orElseThrow();

        assertThat(respuesta.nombre())
                .isEqualTo("Mario Actualizado");

        assertThat(guardado.getNombre())
                .isEqualTo("Mario Actualizado");

        assertThat(guardado.getApellidos())
                .isEqualTo("Apellido Nuevo");

        assertThat(guardado.getDireccion())
                .isEqualTo("Direccion nueva 456");

        // La identidad y el correo no deben cambiar.
        assertThat(guardado.getRun())
                .isEqualTo("123456785");

        assertThat(guardado.getCorreo())
                .isEqualTo("perfil.edicion@gmail.com");
    }

    // Comprueba que actualizar una cuenta no modifica otra.
    // La autorizacion JWT del controlador se prueba aparte.
    @Test
    void actualizarUnPerfilNoModificaOtroUsuario() {
        crearUsuario(
                "123456785",
                "perfil.uno@gmail.com",
                "Mario"
        );

        crearUsuario(
                "876543214",
                "perfil.dos@gmail.com",
                "Segundo"
        );

        PerfilRequest cambios = new PerfilRequest(
                "Mario Modificado",
                "Prueba",
                "Direccion modificada"
        );

        servicio.actualizarPerfil(
                "123456785",
                cambios
        );

        usuarios.flush();

        Usuario primero = usuarios
                .findById("123456785")
                .orElseThrow();

        Usuario segundo = usuarios
                .findById("876543214")
                .orElseThrow();

        assertThat(primero.getNombre())
                .isEqualTo("Mario Modificado");

        assertThat(segundo.getNombre())
                .isEqualTo("Segundo");

        assertThat(segundo.getDireccion())
                .isEqualTo("Direccion original");

        assertThat(segundo.getCorreo())
                .isEqualTo("perfil.dos@gmail.com");
    }

    // Comprueba que la validacion rechaza un nombre vacio.
    @Test
    void rechazaActualizarPerfilConNombreVacio() {
        crearUsuario(
                "123456785",
                "perfil.validacion@gmail.com",
                "Mario"
        );

        PerfilRequest datosInvalidos = new PerfilRequest(
                "",
                "Prueba",
                "Direccion nueva"
        );

        assertThrows(
                ConstraintViolationException.class,
                () -> servicio.actualizarPerfil(
                        "123456785",
                        datosInvalidos
                )
        );

        // Confirma que la cuenta no sufrio modificaciones.
        Usuario guardado = usuarios
                .findById("123456785")
                .orElseThrow();

        assertThat(guardado.getNombre())
                .isEqualTo("Mario");

        assertThat(guardado.getDireccion())
                .isEqualTo("Direccion original");
    }
}
