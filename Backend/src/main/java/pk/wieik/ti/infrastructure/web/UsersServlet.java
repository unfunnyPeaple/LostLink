package pk.wieik.ti.infrastructure.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import pk.wieik.ti.application.UserService;
import pk.wieik.ti.application.dto.UserDto;
import pk.wieik.ti.domain.model.UserRole;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class UsersServlet extends ApiServlet {

    public record IdRequest(UUID id) {}
    public record BlockRequest(UUID id, Boolean blocked) {}
    public record RoleRequest(UUID id, UserRole role) {}

    private final BearerAuth auth;
    private final UserService userService;

    public UsersServlet(BearerAuth auth, UserService userService) {
        this.auth = auth;
        this.userService = userService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        switch (route(req)) {
            case "/me" -> me(req, resp);
            case "/"   -> listUsers(req, resp);
            default    -> throw unknownEndpoint(req);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        switch (route(req)) {
            case "/block" -> setBlocked(req, resp);
            case "/role"  -> changeRole(req, resp);
            default       -> throw unknownEndpoint(req);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        switch (route(req)) {
            case "/delete" -> delete(req, resp);
            default        -> throw unknownEndpoint(req);
        }
    }

    /** GET /api/users/me */
    private void me(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UUID currentUserId = auth.requireUserId(req);

        UserDto user = userService.getCurrentUser(currentUserId);

        ok(resp, user);
    }

    /** GET /api/users (admin) */
    private void listUsers(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UUID currentUserId = auth.requireUserId(req);

        List<UserDto> users = userService.findAll(currentUserId);

        ok(resp, users);
    }

    /** POST /api/users/block (admin) */
    private void setBlocked(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UUID currentUserId = auth.requireUserId(req);
        BlockRequest body = body(req, BlockRequest.class);
        UUID userId = required(body.id(), "id");
        boolean blocked = required(body.blocked(), "blocked");

        UserDto user = userService.setBlocked(currentUserId, userId, blocked);

        ok(resp, user);
    }

    /** POST /api/users/role (admin) */
    private void changeRole(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UUID currentUserId = auth.requireUserId(req);
        RoleRequest body = body(req, RoleRequest.class);
        UUID userId = required(body.id(), "id");
        UserRole role = required(body.role(), "role");

        UserDto user = userService.changeRole(currentUserId, userId, role);

        ok(resp, user);
    }

    /** DELETE /api/users/delete (admin) */
    private void delete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UUID currentUserId = auth.requireUserId(req);
        IdRequest body = body(req, IdRequest.class);
        UUID userId = required(body.id(), "id");

        userService.delete(currentUserId, userId);

        noContent(resp);
    }
}