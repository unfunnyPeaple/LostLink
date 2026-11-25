package pk.wieik.ti.infrastructure.persistence;

import pk.wieik.ti.application.dto.ListingDto;
import pk.wieik.ti.domain.model.listing.Listing;
import pk.wieik.ti.domain.model.listing.ListingCategory;
import pk.wieik.ti.domain.model.listing.ListingStatus;
import pk.wieik.ti.domain.model.listing.ListingType;
import pk.wieik.ti.domain.port.ListingRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SQliteListingRepository implements ListingRepository {
    private static final String TABLE_NAME = "listings";
    private static final String AUTHOR_NAME_COLUMN = "display_name";
    private static final String COLUMNS =
            "id, type, status, category, title, location, date, description, author_id, photo";


    private final Jdbc jdbc;

    public SQliteListingRepository(Jdbc jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Listing> findAllByAuthorId(UUID authorId) {
        return jdbc.query(
                "SELECT " + COLUMNS + " FROM " + TABLE_NAME + " WHERE author_id = ?",
                this::map,
                authorId.toString()
        );
    }

    @Override
    public void save(Listing listing) {
        jdbc.update(
                "INSERT INTO " + TABLE_NAME + " (" + COLUMNS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                listing.id().toString(),
                listing.type().name(),
                listing.status().name(),
                listing.category().name(),
                listing.title(),
                listing.location(),
                listing.date().toString(),
                listing.description(),
                listing.authorId().toString(),
                listing.photo()
        );
    }

    @Override
    public boolean update(Listing listing) {
        int updatedRows = jdbc.update(
                "UPDATE " + TABLE_NAME + " SET type = ?, status = ?, category = ?, title = ?, "
                        + "location = ?, date = ?, description = ?, author_id = ?, photo = ? WHERE id = ?",
                listing.type().name(),
                listing.status().name(),
                listing.category().name(),
                listing.title(),
                listing.location(),
                listing.date().toString(),
                listing.description(),
                listing.authorId().toString(),
                listing.photo(),
                listing.id().toString()
        );
        return updatedRows == 1;
    }

    @Override
    public void delete(UUID listingId) {
        jdbc.update("DELETE FROM " + TABLE_NAME + " WHERE id = ?", listingId.toString());
    }

    @Override
    public Optional<Listing> findByListingId(UUID listingId) {
        return jdbc.queryOne(
                "SELECT " + COLUMNS + " FROM " + TABLE_NAME + " WHERE id = ?",
                this::map,
                listingId.toString()
        );
    }

    @Override
    public List<ListingDto> findAll() {
        return jdbc.query(
                "SELECT " + COLUMNS + ", " + AUTHOR_NAME_COLUMN + " AS display_name FROM " + TABLE_NAME + " INNER JOIN users ON users.id = listings.author_id",
                this::mapToDtoWithAuthor
        );
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
                UUID.fromString(rs.getString("author_id")),
                rs.getString("photo")
        );
    }

    private ListingDto mapToDtoWithAuthor(ResultSet rs) throws SQLException {
        return new ListingDto(
                UUID.fromString(rs.getString("id")),
                ListingType.valueOf(rs.getString("type")),
                ListingStatus.valueOf(rs.getString("status")),
                ListingCategory.valueOf(rs.getString("category")),
                rs.getString("title"),
                rs.getString("location"),
                Instant.parse(rs.getString("date")),
                rs.getString("description"),
                rs.getString("photo"),
                rs.getString("display_name")
        );
    }
}
