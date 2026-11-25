package pk.wieik.ti.infrastructure.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import pk.wieik.ti.application.ListingService;
import pk.wieik.ti.application.command.CreateListingParams;
import pk.wieik.ti.application.command.UpdateListingParams;
import pk.wieik.ti.domain.model.listing.ListingCategory;
import pk.wieik.ti.domain.model.listing.ListingStatus;
import pk.wieik.ti.domain.model.listing.ListingType;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.UUID;

public class ListingServlet extends ApiServlet {
    private final ListingService listingService;
    private final BearerAuth auth;

    private record AddListingRequest(
            ListingType type,
            ListingCategory category,
            String title,
            String location,
            String date,
            String description,
            String photo
    ) {}
    private record UpdateListingRequest(
            UUID id,
            ListingType type,
            ListingStatus status,
            ListingCategory category,
            String title,
            String location,
            String date,
            String description,
            String photo
    ) {}
    private record DeleteListingRequest(UUID id) {};

    public ListingServlet(ListingService listingService, BearerAuth auth) {
        this.listingService = listingService;
        this.auth = auth;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        switch (route(req)) {
            case "/" -> getAllListings(req, resp);
            case "/my" -> getAllUserListings(req, resp);
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

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        switch (route(req)) {
            case "/update" -> updateListing(req, resp);
            default -> throw unknownEndpoint(req);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        switch (route(req)) {
            case "/delete" -> deleteListing(req, resp);
            default -> throw unknownEndpoint(req);
        }
    }

    private void deleteListing(HttpServletRequest req, HttpServletResponse resp) {
        DeleteListingRequest requestId = body(req, DeleteListingRequest.class);

        listingService.deleteListing(
                required(requestId.id(), "id"),
                auth.requireUserId(req)
        );

        noContent(resp);
    }

    private void addListing(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AddListingRequest request = body(req, AddListingRequest.class);
        UUID userId = auth.requireUserId(req);
        listingService.addListing(toCreateListingParams(request, userId));

        noContent(resp);
    }

    private void updateListing(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UpdateListingRequest request = body(req, UpdateListingRequest.class);
        validateUpdateRequest(request);
        UUID userId = auth.requireUserId(req);
        var updatedListing = listingService.updateListing(
                request.id(),
                userId,
                toUpdateListingParams(request)
        );
        ok(resp, updatedListing);
    }

    // No authenticaition needed
    private void getAllListings(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ok(resp, listingService.findAll());
    }

    private void getAllUserListings(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UUID userId = auth.requireUserId(req);
        ok(resp, listingService.findAllByAuthorId(userId));
    }

    // Helpers
    private CreateListingParams toCreateListingParams(AddListingRequest request, UUID userID) {
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
                required(userID, "authorId"),
                request.photo()
        );
    }

    private UpdateListingParams toUpdateListingParams(UpdateListingRequest request) {
        Instant date = null;
        if (request.date() != null) {
            try {
                date = Instant.parse(request.date());
            } catch (DateTimeParseException e) {
                throw HttpError.badRequest("Nieprawidłowa data; oczekiwany format ISO-8601");
            }
        }

        return new UpdateListingParams(
                request.type(),
                request.status(),
                request.category(),
                request.title(),
                request.location(),
                date,
                request.description(),
                request.photo()
        );
    }

    private void validateUpdateRequest(UpdateListingRequest request) {
        if (request.id() == null) {
            throw HttpError.badRequest("Wymagane pole: id");
        }
        if (request.type() == null
                && request.status() == null
                && request.category() == null
                && request.title() == null
                && request.location() == null
                && request.date() == null
                && request.description() == null
                && request.photo() == null) {
            throw HttpError.badRequest("Podaj co najmniej jedno pole do zmiany");
        }
    }
}