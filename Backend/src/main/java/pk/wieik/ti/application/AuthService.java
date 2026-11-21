package pk.wieik.ti.application;

import pk.wieik.ti.application.dto.UserDto;
import pk.wieik.ti.domain.exception.ConflictException;
import pk.wieik.ti.domain.exception.ForbiddenException;
import pk.wieik.ti.domain.exception.UnauthorizedException;
import pk.wieik.ti.domain.exception.ValidationException;
import pk.wieik.ti.domain.model.User;
import pk.wieik.ti.domain.model.UserRole;
import pk.wieik.ti.domain.port.PasswordHasher;
import pk.wieik.ti.domain.port.TokenService;
import pk.wieik.ti.domain.port.UserRepository;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/** Rejestracja i logowanie: walidacja danych, sprawdzanie haseł, wydawanie tokenów. */
public class AuthService {

    private static final Logger LOG = Logger.getLogger(AuthService.class.getName());

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final int MIN_PASSWORD_LENGTH = 12;
    private static final int MAX_DISPLAY_NAME_LENGTH = 64;
    private static final String ADMIN_DISPLAY_NAME = "Administrator";

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenService tokenService;

    public AuthService(UserRepository userRepository, PasswordHasher passwordHasher, TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenService = tokenService;
    }

    public UserDto register(String email, String password, String displayName) {
        User user = createUser(email, password, displayName, UserRole.USER);
        LOG.info("Zarejestrowano użytkownika: " + user.email());
        return UserDto.from(user);
    }

    /** Zwraca token, jeśli e-mail i hasło są poprawne, a konto nie jest zablokowane. */
    public String login(String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        Optional<User> found = userRepository.findByEmail(normalizedEmail)
                .filter(user -> passwordHasher.verify(password, user.passwordHash()));

        if (found.isEmpty()) {
            LOG.warning("Nieudane logowanie: " + normalizedEmail);
            throw new UnauthorizedException("Nieprawidłowy e-mail lub hasło");
        }

        User user = found.get();
        if (user.blocked()) {
            LOG.warning("Próba logowania na zablokowane konto: " + user.email());
            throw new ForbiddenException("Konto jest zablokowane");
        }

        LOG.info("Zalogowano: " + user.email());
        return tokenService.issue(user.id());
    }

    /** Wywoływane przy starcie: tworzy admina, jeśli w bazie nie ma żadnego. */
    public void ensureAdminExists(String email, String password) {
        if (userRepository.existsByRole(UserRole.ADMIN)) {
            return;
        }
        userRepository.findByEmail(normalizeEmail(email)).ifPresentOrElse(
                existing -> {
                    userRepository.setRole(existing.id(), UserRole.ADMIN);
                    LOG.info("Nadano rolę ADMIN istniejącemu kontu: " + existing.email());
                },
                () -> {
                    User admin = createUser(email, password, ADMIN_DISPLAY_NAME, UserRole.ADMIN);
                    LOG.info("Utworzono konto administratora: " + admin.email());
                }
        );
    }

    private User createUser(String email, String password, String displayName, UserRole role) {
        String validEmail = validateEmail(email);
        validatePassword(password);
        String validDisplayName = validateDisplayName(displayName);

        if (userRepository.findByEmail(validEmail).isPresent()) {
            throw new ConflictException("Ten e-mail jest już zajęty");
        }

        User user = new User(
                UUID.randomUUID(),
                validEmail,
                passwordHasher.hash(password),
                validDisplayName,
                role,
                false,
                Instant.now()
        );
        userRepository.save(user);
        return user;
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private static String validateEmail(String email) {
        String normalized = normalizeEmail(email);
        if (!EMAIL.matcher(normalized).matches()) {
            throw new ValidationException("Nieprawidłowy e-mail");
        }
        return normalized;
    }

    private static void validatePassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new ValidationException("Hasło musi mieć co najmniej " + MIN_PASSWORD_LENGTH + " znaków");
        }
    }

    private static String validateDisplayName(String displayName) {
        if (displayName == null || displayName.isBlank() || displayName.trim().length() > MAX_DISPLAY_NAME_LENGTH) {
            throw new ValidationException("Nazwa musi mieć od 1 do " + MAX_DISPLAY_NAME_LENGTH + " znaków");
        }
        return displayName.trim();
    }
}