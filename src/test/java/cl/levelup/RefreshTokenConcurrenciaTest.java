package cl.levelup;

import cl.levelup.auth.RefreshTokenService;
import cl.levelup.usuario.RegistroRequest;
import cl.levelup.usuario.Usuario;
import cl.levelup.usuario.UsuarioService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class RefreshTokenConcurrenciaTest {

    @Autowired
    private UsuarioService usuarios;

    @Autowired
    private RefreshTokenService refreshTokens;

    // Comprueba que solo una solicitud pueda renovar
    // un mismo refresh token cuando llegan simultaneamente.
    @Test
    void soloUnaSolicitudPuedeRenovarElMismoToken()
            throws Exception {

        // Crea un usuario de prueba en la base H2.
        // No utilizamos @Transactional en esta clase:
        // cada hilo debe ejecutar su propia transaccion.
        Usuario usuario = usuarios.registrar(
                new RegistroRequest(
                        "123456785",
                        "Mario",
                        "Concurrencia",
                        "mario.concurrencia@gmail.com",
                        LocalDate.of(2000, 1, 1),
                        "Metropolitana",
                        "Maipu",
                        "Calle de prueba 123",
                        "clave123"
                )
        );

        // Crea un unico refresh token para ambos hilos.
        String token = refreshTokens.crear(usuario);

        // Prepara dos hilos de ejecucion independientes.
        ExecutorService ejecutor =
                Executors.newFixedThreadPool(2);

        // Controla que ambos hilos comiencen juntos.
        CountDownLatch preparados =
                new CountDownLatch(2);

        CountDownLatch inicio =
                new CountDownLatch(1);

        // Cada solicitud intentara renovar el mismo token.
        Callable<Integer> solicitud = () -> {

            preparados.countDown();

            // Espera la señal para comenzar.
            if (!inicio.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException(
                        "No se autorizo el inicio de la prueba."
                );
            }

            try {
                // Intenta renovar el refresh token.
                refreshTokens.renovar(token);

                // Renovacion exitosa.
                return HttpStatus.OK.value();

            } catch (ResponseStatusException error) {

                // Devuelve el codigo HTTP cuando falla.
                return error.getStatusCode().value();
            }
        };

        List<Future<Integer>> resultados =
                new ArrayList<>();

        try {

            // Lanza las dos solicitudes.
            resultados.add(ejecutor.submit(solicitud));
            resultados.add(ejecutor.submit(solicitud));

            // Espera a que ambos hilos esten preparados.
            assertThat(
                    preparados.await(10, TimeUnit.SECONDS)
            ).isTrue();

            // Libera ambas solicitudes simultaneamente.
            inicio.countDown();

            // Recupera los resultados.
            int respuestaA = resultados.get(0)
                    .get(15, TimeUnit.SECONDS);

            int respuestaB = resultados.get(1)
                    .get(15, TimeUnit.SECONDS);

            List<Integer> respuestas =
                    List.of(respuestaA, respuestaB);

            // Solo una renovacion puede tener exito.
            // La otra debe recibir 401 Unauthorized.
            assertThat(respuestas)
                    .containsExactlyInAnyOrder(
                            HttpStatus.OK.value(),
                            HttpStatus.UNAUTHORIZED.value()
                    );

        } finally {

            // Libera cualquier hilo que siga esperando.
            inicio.countDown();

            // Finaliza los hilos utilizados.
            ejecutor.shutdownNow();
        }
    }
}
