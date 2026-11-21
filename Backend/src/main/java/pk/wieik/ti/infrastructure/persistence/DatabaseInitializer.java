package pk.wieik.ti.infrastructure.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

public final class DatabaseInitializer {

    private final SqliteConnectionFactory connections;

    public DatabaseInitializer(SqliteConnectionFactory connections) { this.connections = connections; }

    public void createSchema() { runScript("/db/schema.sql"); }

    public void runScript(String resourcePath) {
        try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
            if (in == null) throw new IllegalStateException("Brak skryptu: " + resourcePath);
            String sql = new String(in.readAllBytes(), StandardCharsets.UTF_8).lines()
                    .filter(line -> !line.trim().startsWith("--"))
                    .collect(Collectors.joining("\n"));

            Files.createDirectories(connections.dbFile().getParent());
            try (Connection connection = connections.open();
                 Statement statement = connection.createStatement()) {
                for (String part : sql.split(";")) {
                    if (!part.isBlank()) statement.execute(part.trim());
                }
            }
        } catch (IOException | SQLException e) {
            throw new PersistenceException("Błąd skryptu " + resourcePath, e);
        }
    }
}