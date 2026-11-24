package pk.wieik.ti.infrastructure.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import pk.wieik.ti.application.ListingService;
import pk.wieik.ti.application.command.CreateListingParams;
import pk.wieik.ti.domain.model.listing.ListingCategory;
import pk.wieik.ti.domain.model.listing.ListingType;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeParseException;

public class ListingServlet extends ApiServlet {
    private final ListingService listingService;

    private record AddListingRequest(
            ListingType type,
            ListingCategory category,
            String title,
            String location,
            String date,
            String description,
            String author,
            String photo
    ) {}

    public ListingServlet(ListingService listingService) {
        this.listingService = listingService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        switch (route(req)) {
            case "/" -> getAllListings(resp);
            default -> throw unknownEndpoint(req);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        switch (route(req)) {
            case "/add" -> addListing(req, resp);
            default -> throw unknownEndpoint(req);
        }
    }

    private void addListing(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AddListingRequest request = body(req, AddListingRequest.class);
        listingService.addListing(toCreateListingParams(request));
        ok(resp, "Dodano listing");
    }

    private void getAllListings(HttpServletResponse resp) throws IOException {
        ok(resp, listingService.findAll());
    }

    // Helpers
    private CreateListingParams toCreateListingParams(AddListingRequest request) {
        Instant date;
        try {
            date = Instant.parse(required(request.date(), "date"));
        } catch (DateTimeParseException e) {
            throw HttpError.badRequest("Nieprawidłowa data; oczekiwany format ISO-8601");
        }

        return new CreateListingParams(
                required(request.type(), "type"),
                required(request.category(), "category"),
                required(request.title(), "title"),
                required(request.location(), "location"),
                date,
                required(request.description(), "description"),
                required(request.author(), "author"),
                request.photo()
        );
    }
}