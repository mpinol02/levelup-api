package cl.levelup.admin;

import cl.levelup.contacto.ContactoRepository;
import cl.levelup.usuario.Rol;
import cl.levelup.usuario.Usuario;
import cl.levelup.usuario.UsuarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReporteService {

    private final UsuarioRepository usuarioRepository;
    private final ContactoRepository contactoRepository;

    public ReporteService(
            UsuarioRepository usuarioRepository,
            ContactoRepository contactoRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.contactoRepository = contactoRepository;
    }

    // Calcula las estadisticas con usuarios reales.
    @Transactional(readOnly = true)
    public ReporteResponse obtenerReporteUsuarios() {

        List<Usuario> usuarios = usuarioRepository.findAll();

        long totalUsuarios = usuarios.size();

        long totalClientes = usuarios.stream()
                .filter(usuario -> usuario.getRol() == Rol.Cliente)
                .count();

        long totalAdministradores = usuarios.stream()
                .filter(usuario ->
                        usuario.getRol() == Rol.Administrador
                )
                .count();

        long totalVendedores = usuarios.stream()
                .filter(usuario -> usuario.getRol() == Rol.Vendedor)
                .count();

        return new ReporteResponse(
                totalUsuarios,
                totalClientes,
                totalAdministradores,
                totalVendedores
        );
    }

    // Calcula las estadisticas de contacto desde la base de datos.
    @Transactional(readOnly = true)
    public ReporteContactoResponse obtenerReporteContactos() {

        long totalContactos = contactoRepository.count();

        long totalPendientes =
                contactoRepository.countByAtendidoFalse();

        long totalAtendidos =
                totalContactos - totalPendientes;

        return new ReporteContactoResponse(
                totalContactos,
                totalPendientes,
                totalAtendidos
        );
    }
}
