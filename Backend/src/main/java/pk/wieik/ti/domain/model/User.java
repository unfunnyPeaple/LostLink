package pk.wieik.ti.domain.model;

import java.time.Instant;
import java.util.UUID;

public record User(
        UUID id,
        String email,
        String passwordHash,
        String displayName,
        UserRole role,
        boolean blocked,
        Instant createdAt
) {}
