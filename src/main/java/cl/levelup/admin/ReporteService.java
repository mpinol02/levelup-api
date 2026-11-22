package cl.levelup.admin;

import cl.levelup.usuario.Rol;
import cl.levelup.usuario.Usuario;
import cl.levelup.usuario.UsuarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReporteService {

    private final UsuarioRepository usuarioRepository;

    public ReporteService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // Calcula las estadísticas con usuarios reales.
    @Transactional(readOnly = true)
    public ReporteResponse obtenerReporteUsuarios() {

        List<Usuario> usuarios = usuarioRepository.findAll();

        long totalUsuarios = usuarios.size();

        long totalClientes = usuarios.stream()
                .filter(usuario -> usuario.getRol() == Rol.Cliente)
                .count();

        long totalAdministradores = usuarios.stream()
                .filter(usuario -> usuario.getRol() == Rol.Administrador)
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
}
