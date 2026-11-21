package pk.wieik.ti.infrastructure.web;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import pk.wieik.ti.domain.exception.*;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ApiServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(ApiServlet.class.getName());
    private static final String ERROR_MESSAGE_ATTRIBUTE = "errorMessage";

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long start = System.nanoTime();
        try {
            super.service(req, resp);
        } catch (HttpError e) {
            error(req, resp, e.status(), e.getMessage());
        } catch (ValidationException e) {
            error(req, resp, 400, e.getMessage());
        } catch (UnauthorizedException e) {
            error(req, resp, 401, e.getMessage());
        } catch (ForbiddenException e) {
            error(req, resp, 403, e.getMessage());
        } catch (NotFoundException e) {
            error(req, resp, 404, e.getMessage());
        } catch (ConflictException e) {
            error(req, resp, 409, e.getMessage());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Nieobsłużony błąd: " + req.getMethod() + " " + req.getRequestURI(), e);
            error(req, resp, 500, "Błąd serwera");
        } finally {
            logRequest(req, resp, start);
        }
    }

    protected String route(HttpServletRequest req) {
        String path = req.getPathInfo();
        return (path == null || path.isEmpty()) ? "/" : path;
    }

    protected <T> T body(HttpServletRequest req, Class<T> type) {
        return Json.read(req, type);
    }

    protected <T> T required(T value, String field) {
        if (value == null) {
            throw HttpError.badRequest("Wymagane pole: " + field);
        }
        return value;
    }

    protected HttpError unknownEndpoint(HttpServletRequest req) {
        return HttpError.notFound("Nieznany endpoint: " + req.getMethod() + " " + req.getRequestURI());
    }

    protected void ok(HttpServletResponse resp, Object body) throws IOException {
        Json.write(resp, 200, body);
    }

    protected void created(HttpServletResponse resp, Object body) throws IOException {
        Json.write(resp, 201, body);
    }

    protected void noContent(HttpServletResponse resp) {
        resp.setStatus(204);
    }

    private void error(HttpServletRequest req, HttpServletResponse resp, int status, String message) throws IOException {
        req.setAttribute(ERROR_MESSAGE_ATTRIBUTE, message);
        Json.write(resp, status, Map.of("error", message));
    }

    private static void logRequest(HttpServletRequest req, HttpServletResponse resp, long start) {
        long durationMs = (System.nanoTime() - start) / 1_000_000;
        Object errorMessage = req.getAttribute(ERROR_MESSAGE_ATTRIBUTE);

        String line = req.getMethod() + " " + req.getRequestURI() + " → " + resp.getStatus() + " (" + durationMs + " ms)"
                + (errorMessage != null ? " – " + errorMessage : "");

        LOG.info(line);
    }
}