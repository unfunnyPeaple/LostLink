package pk.wieik.ti.application.dto;

import pk.wieik.ti.domain.model.User;

public record UserDto(
        String id,
        String email,
        String displayName,
        String role,
        boolean blocked,
        String createdAt
) {
    public static UserDto from(User user) {
        return new UserDto(
                user.id().toString(),
                user.email(),
                user.displayName(),
                user.role().name(),
                user.blocked(),
                user.createdAt().toString()
        );
    }
}