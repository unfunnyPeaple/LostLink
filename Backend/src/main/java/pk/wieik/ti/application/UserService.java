package pk.wieik.ti.application;

import pk.wieik.ti.application.dto.UserDto;
import pk.wieik.ti.domain.exception.ForbiddenException;
import pk.wieik.ti.domain.exception.NotFoundException;
import pk.wieik.ti.domain.exception.UnauthorizedException;
import pk.wieik.ti.domain.model.User;
import pk.wieik.ti.domain.model.UserRole;
import pk.wieik.ti.domain.port.UserRepository;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/** Dane użytkowników i akcje administratora. */
public class UserService {

    private static final Logger LOG = Logger.getLogger(UserService.class.getName());

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDto getCurrentUser(UUID currentUserId) {
        return UserDto.from(loadActiveUser(currentUserId));
    }

    public List<UserDto> findAll(UUID currentUserId) {
        requireAdmin(currentUserId);
        return userRepository.findAll().stream().map(UserDto::from).toList();
    }

    public UserDto setBlocked(UUID currentUserId, UUID userId, boolean blocked) {
        requireAdmin(currentUserId);
        requireNotSelf(currentUserId, userId, "zablokować");
        requireExists(userId);

        userRepository.setBlocked(userId, blocked);
        LOG.info("Admin " + currentUserId + (blocked ? " zablokował" : " odblokował") + " użytkownika " + userId);
        return UserDto.from(findById(userId));
    }

    public UserDto changeRole(UUID currentUserId, UUID userId, UserRole role) {
        requireAdmin(currentUserId);
        requireNotSelf(currentUserId, userId, "zmienić roli");
        requireExists(userId);

        userRepository.setRole(userId, role);
        LOG.info("Admin " + currentUserId + " zmienił rolę użytkownika " + userId + " na " + role);
        return UserDto.from(findById(userId));
    }

    public void delete(UUID currentUserId, UUID userId) {
        requireAdmin(currentUserId);
        requireNotSelf(currentUserId, userId, "usunąć");
        requireExists(userId);

        userRepository.delete(userId);
        LOG.info("Admin " + currentUserId + " usunął użytkownika " + userId);
    }

    /** Konto wykonujące akcję: musi istnieć i nie może być zablokowane. */
    private User loadActiveUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("Konto nie istnieje"));
        if (user.blocked()) {
            throw new ForbiddenException("Konto jest zablokowane");
        }
        return user;
    }

    private void requireAdmin(UUID currentUserId) {
        if (loadActiveUser(currentUserId).role() != UserRole.ADMIN) {
            throw new ForbiddenException("Wymagane uprawnienia administratora");
        }
    }

    private static void requireNotSelf(UUID currentUserId, UUID userId, String action) {
        if (currentUserId.equals(userId)) {
            throw new ForbiddenException("Nie możesz " + action + " własnego konta");
        }
    }

    private void requireExists(UUID userId) {
        findById(userId);
    }

    private User findById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Nie ma użytkownika o id " + userId));
    }
}