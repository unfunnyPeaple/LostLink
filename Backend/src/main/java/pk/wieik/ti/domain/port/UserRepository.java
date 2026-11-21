package pk.wieik.ti.domain.port;

import pk.wieik.ti.domain.model.User;
import pk.wieik.ti.domain.model.UserRole;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    List<User> findAll();
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);
    boolean existsByRole(UserRole role);

    void save(User user);
    void setBlocked(UUID id, boolean blocked);
    void setRole(UUID id, UserRole role);
    void delete(UUID id);
}