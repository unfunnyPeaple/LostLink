package pk.wieik.ti.infrastructure.web;

public class HttpError extends RuntimeException {

    private final int status;

    public HttpError(int status, String message) {
        super(message);
        this.status = status;
    }

    public int status() { return status; }

    public static HttpError badRequest(String message)   { return new HttpError(400, message); }
    public static HttpError unauthorized(String message) { return new HttpError(401, message); }
    public static HttpError forbidden(String message)    { return new HttpError(403, message); }
    public static HttpError notFound(String message)     { return new HttpError(404, message); }
}