package pk.wieik.ti.application.command;

import pk.wieik.ti.domain.model.listing.ListingCategory;
import pk.wieik.ti.domain.model.listing.ListingType;

import java.time.Instant;
import java.util.UUID;

public record CreateListingParams(
        ListingType type,
        ListingCategory category,
        String title,
        String location,
        Instant date,
        String description,
        UUID authorId,
        String photo
) {}
