package pk.wieik.ti.infrastructure.config;

import io.github.cdimascio.dotenv.Dotenv;

import java.nio.file.Path;
import java.time.Duration;

/** Ustawienia aplikacji: zmienne środowiskowe, a gdy ich brak - plik .env. */
public record AppConfig(
        Path dbPath,
        String jwtSecret,
        Duration jwtTtl,
        String adminEmail,
        String adminPassword
) {
    private static final Dotenv DOTENV = Dotenv.configure().ignoreIfMissing().load();
    private static final int MIN_JWT_SECRET_LENGTH = 32;

    public static AppConfig load() {
        return new AppConfig(
                readDbPath(),
                readJwtSecret(),
                Duration.ofMinutes(intOr("JWT_TTL_MINUTES", 60)),
                required("ADMIN_EMAIL"),
                required("ADMIN_PASSWORD")
        );
    }

    private static String readJwtSecret() {
        String secret = required("JWT_SECRET");
        if (secret.length() < MIN_JWT_SECRET_LENGTH) {
            throw new IllegalStateException("JWT_SECRET musi mieć co najmniej " + MIN_JWT_SECRET_LENGTH + " znaki");
        }
        return secret;
    }

    private static Path readDbPath() {
        Path path = Path.of(required("DB_PATH"));
        if (!path.isAbsolute()) {
            path = Path.of(System.getProperty("catalina.base", ".")).resolve(path);
        }
        return path.toAbsolutePath().normalize();
    }

    private static String get(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) value = DOTENV.get(key);
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private static String required(String key) {
        String value = get(key);
        if (value == null) throw new IllegalStateException("Brak wymaganej zmiennej: " + key);
        return value;
    }

    private static int intOr(String key, int defaultValue) {
        String value = get(key);
        return value == null ? defaultValue : Integer.parseInt(value);
    }
}