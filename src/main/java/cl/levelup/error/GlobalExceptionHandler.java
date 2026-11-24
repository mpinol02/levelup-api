package cl.levelup.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

// Centraliza las respuestas de error de los controladores REST.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Devuelve 400 cuando un campo no cumple sus validaciones.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> manejarValidaciones(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {

        Map<String, String> errores = new LinkedHashMap<>();

        // Reune los mensajes de los campos invalidos.
        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error -> errores.putIfAbsent(
                        error.getField(),
                        error.getDefaultMessage() == null
                                ? "Valor invalido"
                                : error.getDefaultMessage()
                ));

        ApiError respuesta = ApiError.conCampos(
                400,
                "Bad Request",
                "La solicitud contiene campos invalidos",
                request.getRequestURI(),
                errores
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(respuesta);
    }

    // Devuelve 400 cuando falla una validacion de parametros.
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> manejarRestricciones(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {

        Map<String, String> errores = new LinkedHashMap<>();

        exception.getConstraintViolations()
                .forEach(error -> errores.put(
                        error.getPropertyPath().toString(),
                        error.getMessage()
                ));

        ApiError respuesta = ApiError.conCampos(
                400,
                "Bad Request",
                "Uno o mas parametros son invalidos",
                request.getRequestURI(),
                errores
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(respuesta);
    }

    // Devuelve 400 cuando el JSON esta mal formado o tiene datos invalidos.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> manejarJsonInvalido(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {

        ApiError respuesta = ApiError.crear(
                400,
                "Bad Request",
                "El cuerpo de la solicitud no contiene un JSON valido",
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(respuesta);
    }

    // Respeta los errores HTTP definidos en los servicios.
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> manejarEstadoHttp(
            ResponseStatusException exception,
            HttpServletRequest request
    ) {

        int codigo = exception.getStatusCode().value();

        String mensaje = exception.getReason() == null
                ? "No se pudo completar la solicitud"
                : exception.getReason();

        String tipoError = HttpStatus.resolve(codigo) == null
                ? "Error HTTP"
                : HttpStatus.resolve(codigo).getReasonPhrase();

        ApiError respuesta = ApiError.crear(
                codigo,
                tipoError,
                mensaje,
                request.getRequestURI()
        );

        return ResponseEntity
                .status(exception.getStatusCode())
                .body(respuesta);
    }

    // Devuelve 409 cuando una restriccion de la base de datos falla.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> manejarConflictoBaseDatos(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {

        ApiError respuesta = ApiError.crear(
                409,
                "Conflict",
                "La operacion no se puede completar por una restriccion de datos",
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(respuesta);
    }
}
