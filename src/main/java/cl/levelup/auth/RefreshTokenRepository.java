package cl.levelup.auth;

import java.time.Instant;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// Permite consultar y administrar los tokens de renovacion.
public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, Long> {

    // Bloquea el registro mientras se valida y revoca el token.
    // Evita que dos transacciones lo consuman simultaneamente.
    // Debe ejecutarse dentro de una transaccion.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Query("SELECT t.usuario.run FROM RefreshToken t WHERE t.tokenHash = :hash")
    Optional<String> buscarRunPorHash(@Param("hash") String hash);

    @Modifying
    @Query("DELETE FROM RefreshToken t WHERE t.usuario.run = :run")
    void eliminarPorRun(@Param("run") String run);

    // Elimina registros vencidos o revocados.
    // Debe ejecutarse dentro de una transaccion.
    @Modifying
    @Query("""
            DELETE FROM RefreshToken t
            WHERE t.venceEn <= :ahora
               OR t.revocadoEn IS NOT NULL
            """)
    int eliminarTokensInactivos(@Param("ahora") Instant ahora);
}
