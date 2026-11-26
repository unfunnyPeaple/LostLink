package pk.wieik.ti.domain.port;

import pk.wieik.ti.application.dto.ListingDto;
import pk.wieik.ti.domain.model.listing.Listing;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ListingRepository {
    List<Listing> findAllByAuthorId(UUID authorId);
    void save(Listing listing);
    boolean update(Listing listing);
    boolean delete(UUID listingId);
    Optional<Listing> findByListingId(UUID listingId);
    List<ListingDto> findAll();
}
