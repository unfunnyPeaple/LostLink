package pk.wieik.ti.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public final class Json {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Json() {}

    public static <T> T read(HttpServletRequest req, Class<T> type) {
        try {
            T value = MAPPER.readValue(req.getInputStream(), type);
            if (value == null) throw HttpError.badRequest("Puste body");
            return value;
        } catch (InvalidFormatException e) {
            String field = e.getPath().isEmpty() ? "?" : e.getPath().get(0).getFieldName();
            throw HttpError.badRequest("Nieprawidłowa wartość pola: " + field);
        } catch (IOException e) {
            throw HttpError.badRequest("Nieprawidłowy JSON");
        }
    }

    public static void write(HttpServletResponse resp, int status, Object body) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        MAPPER.writeValue(resp.getWriter(), body);
    }
}