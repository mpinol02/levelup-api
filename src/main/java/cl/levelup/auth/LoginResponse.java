package cl.levelup.auth;

// Respuesta de autenticacion enviada al frontend React.
public record LoginResponse(

        // JWT utilizado para acceder a endpoints protegidos.
        String accessToken,

        // Tipo de autenticacion utilizado por Spring Security.
        String tokenType,

        // Duracion del JWT expresada en segundos.
        long expiresIn,

        // Token utilizado para solicitar una nueva sesion.
        String refreshToken

) {

    // Mantiene compatibilidad con el constructor anterior.
    // Se retirara cuando el controlador entregue ambos tokens.
    public LoginResponse(
            String accessToken,
            String tokenType,
            long expiresIn
    ) {
        this(accessToken, tokenType, expiresIn, null);
    }
}
