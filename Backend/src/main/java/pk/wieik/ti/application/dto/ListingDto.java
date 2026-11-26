package pk.wieik.ti.application.dto;

import pk.wieik.ti.domain.model.listing.Listing;
import pk.wieik.ti.domain.model.listing.ListingCategory;
import pk.wieik.ti.domain.model.listing.ListingStatus;
import pk.wieik.ti.domain.model.listing.ListingType;

import java.time.Instant;
import java.util.UUID;

public record ListingDto(
    UUID id,
    ListingType type,
    ListingStatus status,
    ListingCategory category,
    String title,
    String location,
    Instant date,
    String description,
    String photo,
    String displayName
) {
    public static ListingDto toListingDto(Listing listing) {
        return new ListingDto(
                listing.id(),
                listing.type(),
                listing.status(),
                listing.category(),
                listing.title(),
                listing.location(),
                listing.date(),
                listing.description(),
                listing.photo(),
                null
        );
    }
}
