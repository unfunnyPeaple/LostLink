package pk.wieik.ti.infrastructure.persistence;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class Jdbc {

    @FunctionalInterface
    public interface RowMapper<T> { T map(ResultSet rs) throws SQLException; }

    @FunctionalInterface
    private interface StatementCallback<R> { R apply(PreparedStatement ps) throws SQLException; }

    private final SqliteConnectionFactory connections;

    public Jdbc(SqliteConnectionFactory connections) { this.connections = connections; }

    public <T> List<T> query(String sql, RowMapper<T> mapper, Object... params) {
        return execute(sql, params, ps -> {
            try (ResultSet rs = ps.executeQuery()) {
                List<T> rows = new ArrayList<>();
                while (rs.next()) rows.add(mapper.map(rs));
                return rows;
            }
        });
    }

    public <T> Optional<T> queryOne(String sql, RowMapper<T> mapper, Object... params) {
        return query(sql, mapper, params).stream().findFirst();
    }

    public int update(String sql, Object... params) {
        return execute(sql, params, PreparedStatement::executeUpdate);
    }

    public long insert(String sql, Object... params) {
        return execute(sql, params, ps -> {
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        });
    }

    private <R> R execute(String sql, Object[] params, StatementCallback<R> callback) {
        try (Connection connection = connections.open();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            return callback.apply(ps);
        } catch (SQLException e) {
            throw new PersistenceException("Błąd zapytania SQL: " + sql, e);
        }
    }
}