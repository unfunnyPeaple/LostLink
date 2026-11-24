package pk.wieik.ti.application;

import pk.wieik.ti.application.command.CreateListingParams;
import pk.wieik.ti.application.dto.ListingDto;
import pk.wieik.ti.domain.port.ListingRepository;

import java.util.List;

public class ListingService {
    private final ListingRepository listingRepository;

    public ListingService(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    public List<ListingDto> findAll() {
        return listingRepository.findAll().stream().map(ListingDto::toListingDto).toList();
    }

    public void addListing(CreateListingParams listingParams) {
        listingRepository.save(listingParams);
    }
}
