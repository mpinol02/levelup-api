package cl.levelup.contacto;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

@Service
@Validated
public class ContactoService {

    private static final Set<String> DOMINIOS_PERMITIDOS =
            Set.of(
                    "gmail.com",
                    "duoc.cl",
                    "profesor.duoc.cl"
            );

    private final ContactoRepository contactos;

    public ContactoService(
            ContactoRepository contactos
    ) {
        this.contactos = contactos;
    }

    // Registra una consulta nueva en la base de datos.
    @Transactional
    public ContactoResponse registrar(
            @Valid ContactoRequest datos
    ) {
        String nombre = datos.nombre().trim();

        String correo = datos.correo() == null
                ? ""
                : datos.correo().trim()
                    .toLowerCase(Locale.ROOT);

        String comentario = datos.comentario().trim();

        // El correo es opcional.
        if (!correo.isBlank()) {

            String dominio = correo.substring(
                    correo.lastIndexOf('@') + 1
            );

            if (!DOMINIOS_PERMITIDOS.contains(dominio)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Solo se permiten correos Duoc o Gmail."
                );
            }
        }

        Contacto nuevo = new Contacto(
                nombre,
                correo.isBlank() ? null : correo,
                comentario
        );

        Contacto guardado = contactos.saveAndFlush(nuevo);

        return ContactoResponse.desdeContacto(guardado);
    }

    // Consulta todos los mensajes recibidos.
    @Transactional(readOnly = true)
    public List<ContactoResponse> listar() {

        return contactos
                .findAllByOrderByFechaEnvioDesc()
                .stream()
                .map(ContactoResponse::desdeContacto)
                .toList();
    }

    // Consulta un mensaje especifico.
    @Transactional(readOnly = true)
    public ContactoResponse consultar(Long id) {

        return ContactoResponse.desdeContacto(
                buscarContacto(id)
        );
    }

    // Actualiza el estado de atencion de una consulta.
    @Transactional
    public ContactoResponse actualizarEstado(
            Long id,
            boolean atendido
    ) {
        Contacto contacto = buscarContacto(id);

        contacto.setAtendido(atendido);

        contactos.saveAndFlush(contacto);

        return ContactoResponse.desdeContacto(contacto);
    }

    // Elimina un mensaje existente.
    @Transactional
    public void eliminar(Long id) {

        Contacto contacto = buscarContacto(id);

        contactos.delete(contacto);
        contactos.flush();
    }

    // Cuenta los mensajes que todavia no se atienden.
    @Transactional(readOnly = true)
    public long contarPendientes() {

        return contactos.countByAtendidoFalse();
    }

    // Centraliza la busqueda de mensajes.
    private Contacto buscarContacto(Long id) {

        return contactos.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "No se encontro la consulta."
                        )
                );
    }
}
