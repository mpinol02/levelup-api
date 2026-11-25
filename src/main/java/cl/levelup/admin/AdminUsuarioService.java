package cl.levelup.admin;

import java.util.List;

import cl.levelup.auth.RefreshTokenRepository;
import cl.levelup.usuario.RegistroRequest;
import cl.levelup.usuario.Rol;
import cl.levelup.usuario.Usuario;
import cl.levelup.usuario.UsuarioRepository;
import cl.levelup.usuario.UsuarioRequest;
import cl.levelup.usuario.UsuarioResponse;
import cl.levelup.usuario.UsuarioService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

@Service
@Validated
public class AdminUsuarioService {

    private final UsuarioRepository usuarios;
    private final UsuarioService usuarioService;
    private final RefreshTokenRepository refreshTokens;

    public AdminUsuarioService(
            UsuarioRepository usuarios,
            UsuarioService usuarioService,
            RefreshTokenRepository refreshTokens
    ) {
        this.usuarios = usuarios;
        this.usuarioService = usuarioService;
        this.refreshTokens = refreshTokens;
    }

    // Lista usuarios sin exponer datos sensibles.
    @Transactional(readOnly = true)
    public List<AdminUsuarioResponse> listar() {

        return usuarios.findAll(
                Sort.by(Sort.Direction.ASC, "nombre")
        )
                .stream()
                .map(usuario -> new AdminUsuarioResponse(
                        usuario.getRun(),
                        usuario.getNombre(),
                        usuario.getApellidos(),
                        usuario.getCorreo(),
                        usuario.getRol()
                ))
                .toList();
    }

    // Consulta el detalle de una cuenta.
    @Transactional(readOnly = true)
    public UsuarioResponse consultar(String run) {

        return UsuarioResponse.desdeUsuario(
                buscarUsuario(run)
        );
    }

    // Registra una cuenta reutilizando las validaciones existentes.
    // Las cuentas nuevas se crean inicialmente como Cliente.
    @Transactional
    public UsuarioResponse crear(
            @Valid RegistroRequest datos
    ) {
        Usuario usuario = usuarioService.registrar(datos);

        usuarios.flush();

        return UsuarioResponse.desdeUsuario(usuario);
    }

    // Actualiza los datos administrativos de una cuenta.
    @Transactional
    public UsuarioResponse actualizar(
            String run,
            String administradorRun,
            @Valid UsuarioRequest datos
    ) {
        Usuario usuario = usuarios.bloquearPorRun(run).orElseThrow(this::usuarioNoEncontrado);

        // Impide que el administrador elimine su propio permiso.
        if (
                usuario.getRun().equals(administradorRun)
                && datos.rol() != Rol.Administrador
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No puedes quitar tu propio rol de Administrador."
            );
        }

        usuario.setNombre(datos.nombre().trim());
        usuario.setApellidos(datos.apellidos().trim());
        usuario.setDireccion(datos.direccion().trim());
        if (usuario.getRol() != datos.rol()) {
            usuario.setRol(datos.rol());
            refreshTokens.eliminarPorRun(run);
        }

        usuarios.saveAndFlush(usuario);

        return UsuarioResponse.desdeUsuario(usuario);
    }

    // Elimina cuentas existentes, excepto la propia.
    @Transactional
    public void eliminar(
            String run,
            String administradorRun
    ) {
        Usuario usuario = usuarios.bloquearPorRun(run).orElseThrow(this::usuarioNoEncontrado);

        if (usuario.getRun().equals(administradorRun)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No puedes eliminar tu propia cuenta."
            );
        }

        refreshTokens.eliminarPorRun(run);
        usuarios.delete(usuario);
        usuarios.flush();
    }

    // Centraliza la busqueda y el manejo de usuarios inexistentes.
    private Usuario buscarUsuario(String run) {

        return usuarios.findById(run)
                .orElseThrow(this::usuarioNoEncontrado);
    }

    private ResponseStatusException usuarioNoEncontrado() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontro el usuario.");
    }
}
