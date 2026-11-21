package pk.wieik.ti.infrastructure.persistence;

import pk.wieik.ti.domain.model.User;
import pk.wieik.ti.domain.model.UserRole;
import pk.wieik.ti.domain.port.UserRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SqliteUserRepository implements UserRepository {

    private static final String COLUMNS =
            "id, email, password_hash, display_name, user_role, is_blocked, created_at";

    private final Jdbc jdbc;

    public SqliteUserRepository(Jdbc jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<User> findAll() {
        return jdbc.query("SELECT " + COLUMNS + " FROM users ORDER BY created_at", this::map);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jdbc.queryOne("SELECT " + COLUMNS + " FROM users WHERE id = ?", this::map, id.toString());
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jdbc.queryOne("SELECT " + COLUMNS + " FROM users WHERE email = ?", this::map, email);
    }

    @Override
    public boolean existsByRole(UserRole role) {
        return jdbc.queryOne("SELECT 1 FROM users WHERE user_role = ? LIMIT 1", rs -> true, role.name())
                .isPresent();
    }

    @Override
    public void save(User user) {
        jdbc.update("INSERT INTO users (" + COLUMNS + ") VALUES (?, ?, ?, ?, ?, ?, ?)",
                user.id().toString(),
                user.email(),
                user.passwordHash(),
                user.displayName(),
                user.role().name(),
                user.blocked() ? 1 : 0,
                user.createdAt().toString());
    }


    @Override
    public void setBlocked(UUID id, boolean blocked) {
        jdbc.update("UPDATE users SET is_blocked = ? WHERE id = ?", blocked ? 1 : 0, id.toString());
    }

    @Override
    public void setRole(UUID id, UserRole role) {
        jdbc.update("UPDATE users SET user_role = ? WHERE id = ?", role.name(), id.toString());
    }

    @Override
    public void delete(UUID id) {
        jdbc.update("DELETE FROM users WHERE id = ?", id.toString());
    }

    private User map(ResultSet rs) throws SQLException {
        return new User(
                UUID.fromString(rs.getString("id")),
                rs.getString("email"),
                rs.getString("password_hash"),
                rs.getString("display_name"),
                UserRole.valueOf(rs.getString("user_role")),
                rs.getInt("is_blocked") == 1,
                Instant.parse(rs.getString("created_at"))
        );
    }
}