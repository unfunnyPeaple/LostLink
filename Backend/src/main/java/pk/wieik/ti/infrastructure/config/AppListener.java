package pk.wieik.ti.infrastructure.config;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpServlet;
import pk.wieik.ti.application.AuthService;
import pk.wieik.ti.application.UserService;
import pk.wieik.ti.domain.port.PasswordHasher;
import pk.wieik.ti.domain.port.TokenService;
import pk.wieik.ti.domain.port.UserRepository;
import pk.wieik.ti.infrastructure.persistence.DatabaseInitializer;
import pk.wieik.ti.infrastructure.persistence.Jdbc;
import pk.wieik.ti.infrastructure.persistence.SqliteConnectionFactory;
import pk.wieik.ti.infrastructure.persistence.SqliteUserRepository;
import pk.wieik.ti.infrastructure.security.JwtTokenService;
import pk.wieik.ti.infrastructure.security.Pbkdf2PasswordHasher;
import pk.wieik.ti.infrastructure.web.AuthServlet;
import pk.wieik.ti.infrastructure.web.BearerAuth;
import pk.wieik.ti.infrastructure.web.HealthServlet;
import pk.wieik.ti.infrastructure.web.UsersServlet;

import java.util.logging.Logger;

@WebListener
public class AppListener implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(AppListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        AppConfig config = AppConfig.load();

        // Baza danych
        SqliteConnectionFactory connections = new SqliteConnectionFactory(config.dbPath());
        new DatabaseInitializer(connections).createSchema();
        Jdbc jdbc = new Jdbc(connections);

        // Porty
        UserRepository userRepository = new SqliteUserRepository(jdbc);
        PasswordHasher passwordHasher = new Pbkdf2PasswordHasher();
        TokenService tokenService = new JwtTokenService(config.jwtSecret(), config.jwtTtl());

        // Serwisy
        AuthService authService = new AuthService(userRepository, passwordHasher, tokenService);
        UserService userService = new UserService(userRepository);

        authService.ensureAdminExists(config.adminEmail(), config.adminPassword());

        // Kontrolery
        BearerAuth bearerAuth = new BearerAuth(tokenService);
        ServletContext ctx = sce.getServletContext();
        mount(ctx, "/api/health", new HealthServlet());
        mount(ctx, "/api/auth/*", new AuthServlet(authService));
        mount(ctx, "/api/users/*", new UsersServlet(bearerAuth, userService));

        LOG.info("LostLink uruchomiony. Baza: " + config.dbPath() + ", ważność tokenu: " + config.jwtTtl().toMinutes() + " min");
    }

    private static void mount(ServletContext ctx, String path, HttpServlet servlet) {
        ctx.addServlet(servlet.getClass().getSimpleName(), servlet).addMapping(path);
    }
}