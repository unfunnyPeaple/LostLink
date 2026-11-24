package pk.wieik.ti.infrastructure.persistence;

import pk.wieik.ti.application.command.CreateListingParams;
import pk.wieik.ti.domain.model.listing.Listing;
import pk.wieik.ti.domain.model.listing.ListingCategory;
import pk.wieik.ti.domain.model.listing.ListingStatus;
import pk.wieik.ti.domain.model.listing.ListingType;
import pk.wieik.ti.domain.port.ListingRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class SQliteListingRepository implements ListingRepository {
    private static final String TABLE_NAME = "listings";
    private static final String COLUMNS =
            "id, type, status, category, title, location, date, description, author, photo";

    private final Jdbc jdbc;

    public SQliteListingRepository(Jdbc jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Listing> findAll() {
        return jdbc.query("SELECT " + COLUMNS + " FROM " + TABLE_NAME, this::map);
    }

    private Listing map(ResultSet rs) throws SQLException {
        return new Listing(
                UUID.fromString(rs.getString("id")),
                ListingType.valueOf(rs.getString("type")),
                ListingStatus.valueOf(rs.getString("status")),
                ListingCategory.valueOf(rs.getString("category")),
                rs.getString("title"),
                rs.getString("location"),
                Instant.parse(rs.getString("date")),
                rs.getString("description"),
                rs.getString("author"),
                rs.getString("photo")
        );
    }

    @Override
    public void save(CreateListingParams listingParams) {
        jdbc.update(
                "INSERT INTO " + TABLE_NAME + " (" + COLUMNS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                UUID.randomUUID().toString(),
                listingParams.type().name(),
                ListingStatus.ACTIVE.name(),
                listingParams.category().name(),
                listingParams.title(),
                listingParams.location(),
                listingParams.date().toString(),
                listingParams.description(),
                listingParams.author(),
                listingParams.photo()
        );
    }
}
