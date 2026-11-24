package pk.wieik.ti.domain.port;

import pk.wieik.ti.application.command.CreateListingParams;
import pk.wieik.ti.domain.model.listing.Listing;

import java.util.List;
import java.util.Optional;

public interface ListingRepository {
    List<Listing> findAll();
    void save(CreateListingParams listingParams);
}
