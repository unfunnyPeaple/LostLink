package pk.wieik.ti.infrastructure.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import pk.wieik.ti.application.AuthService;
import pk.wieik.ti.application.dto.UserDto;

import java.io.IOException;

public class AuthServlet extends ApiServlet {

    public record RegisterRequest(String email, String password, String displayName) {}
    public record LoginRequest(String email, String password) {}

    private final AuthService authService;

    public AuthServlet(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        switch (route(req)) {
            case "/register" -> register(req, resp);
            case "/login"    -> login(req, resp);
            default          -> throw unknownEndpoint(req);
        }
    }

    /** POST /api/auth/register */
    private void register(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RegisterRequest body = body(req, RegisterRequest.class);

        UserDto user = authService.register(body.email(), body.password(), body.displayName());

        created(resp, user);
    }

    /** POST /api/auth/login */
    private void login(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        LoginRequest body = body(req, LoginRequest.class);

        String token = authService.login(body.email(), body.password());

        ok(resp, new LoginResponse(token));
    }
}