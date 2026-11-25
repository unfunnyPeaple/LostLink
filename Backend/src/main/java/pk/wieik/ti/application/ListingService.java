package pk.wieik.ti.application;

import pk.wieik.ti.application.command.CreateListingParams;
import pk.wieik.ti.application.command.UpdateListingParams;
import pk.wieik.ti.application.dto.ListingDto;
import pk.wieik.ti.domain.exception.ForbiddenException;
import pk.wieik.ti.domain.exception.NotFoundException;
import pk.wieik.ti.domain.model.listing.Listing;
import pk.wieik.ti.domain.model.listing.ListingStatus;
import pk.wieik.ti.domain.port.ListingRepository;

import java.util.List;
import java.util.UUID;

public class ListingService {
    private final ListingRepository listingRepository;

    public ListingService(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    public List<ListingDto> findAllByAuthorId(UUID authorId) {
        return listingRepository.findAllByAuthorId(authorId).stream().map(ListingDto::toListingDto).toList();
    }

    public void addListing(CreateListingParams listingParams) {
        Listing listing = new Listing(
                UUID.randomUUID(),
                listingParams.type(),
                ListingStatus.ACTIVE,
                listingParams.category(),
                listingParams.title(),
                listingParams.location(),
                listingParams.date(),
                listingParams.description(),
                listingParams.authorId(),
                listingParams.photo()
        );
        listingRepository.save(listing);
    }

    public ListingDto updateListing(UUID listingId, UUID userId, UpdateListingParams params) {
        Listing existingListing = requireOwnership(listingId, userId);
        Listing updatedListing = new Listing(
                existingListing.id(),
                params.type() != null ? params.type() : existingListing.type(),
                params.status() != null ? params.status() : existingListing.status(),
                params.category() != null ? params.category() : existingListing.category(),
                params.title() != null ? params.title() : existingListing.title(),
                params.location() != null ? params.location() : existingListing.location(),
                params.date() != null ? params.date() : existingListing.date(),
                params.description() != null ? params.description() : existingListing.description(),
                existingListing.authorId(),
                params.photo() != null ? params.photo() : existingListing.photo()
        );

        if (!listingRepository.update(updatedListing)) {
            throw new NotFoundException("Nie ma listingu o podanym id");
        }

        return ListingDto.toListingDto(updatedListing);
    }

    public void deleteListing(UUID listingId, UUID userId) {
        requireOwnership(listingId, userId);
        listingRepository.delete(listingId);
    }

    public List<ListingDto> findAll() {
        return listingRepository.findAll();
    }


    private Listing requireOwnership(UUID listingId, UUID userId) {
        Listing listing = listingRepository.findByListingId(listingId).orElseThrow(
                () -> new NotFoundException("Nie ma listingu o podanym id")
        );

        if (!listing.authorId().equals(userId)) {
            throw new ForbiddenException("Użytkownik nie posiada takiego listingu");
        }
        return listing;
    }

}
