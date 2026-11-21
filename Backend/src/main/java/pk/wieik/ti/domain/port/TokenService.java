package pk.wieik.ti.domain.port;

import java.util.Optional;
import java.util.UUID;

public interface TokenService {

    /** Wydaje token dla użytkownika. */
    String issue(UUID userId);

    /** Id użytkownika z poprawnego, niewygasłego tokenu; pusty, gdy token jest zły. */
    Optional<UUID> verify(String token);
}