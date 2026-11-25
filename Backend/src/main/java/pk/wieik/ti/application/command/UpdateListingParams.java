package pk.wieik.ti.application.command;

import pk.wieik.ti.domain.model.listing.ListingCategory;
import pk.wieik.ti.domain.model.listing.ListingStatus;
import pk.wieik.ti.domain.model.listing.ListingType;

import java.time.Instant;

public record UpdateListingParams(
        ListingType type,
        ListingStatus status,
        ListingCategory category,
        String title,
        String location,
        Instant date,
        String description,
        String photo
) {}
