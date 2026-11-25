package pk.wieik.ti.domain.model.listing;

import java.time.Instant;
import java.util.UUID;

public record Listing(
        UUID id,
        ListingType type,
        ListingStatus status,
        ListingCategory category,
        String title,
        String location,
        Instant date,
        String description,
        UUID authorId,
        String photo
) {}
