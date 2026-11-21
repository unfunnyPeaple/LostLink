package pk.wieik.ti.infrastructure.web;

import jakarta.servlet.http.HttpServletRequest;
import pk.wieik.ti.domain.port.TokenService;

import java.util.UUID;

/** Odczyt i weryfikacja tokenu z nagłówka "Authorization: Bearer <token>". */
public final class BearerAuth {

    private static final String PREFIX = "Bearer ";

    private final TokenService tokenService;

    public BearerAuth(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    /** Id zalogowanego użytkownika; brak lub zły token → 401. */
    public UUID requireUserId(HttpServletRequest req) {
        String header = req.getHeader("Authorization");
        if (header == null || !header.startsWith(PREFIX)) {
            throw HttpError.unauthorized("Brak tokenu");
        }
        String token = header.substring(PREFIX.length()).trim();
        return tokenService.verify(token)
                .orElseThrow(() -> HttpError.unauthorized("Nieprawidłowy lub wygasły token"));
    }
}