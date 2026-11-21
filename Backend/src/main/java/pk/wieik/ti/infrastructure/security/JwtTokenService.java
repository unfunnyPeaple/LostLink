package pk.wieik.ti.infrastructure.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import pk.wieik.ti.domain.port.TokenService;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public final class JwtTokenService implements TokenService {

    private static final String ISSUER = "lostlink";

    private final Algorithm algorithm;
    private final JWTVerifier verifier;
    private final Duration ttl;

    public JwtTokenService(String secret, Duration ttl) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.verifier = JWT.require(algorithm).withIssuer(ISSUER).build();
        this.ttl = ttl;
    }

    @Override
    public String issue(UUID userId) {
        Instant now = Instant.now();
        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(userId.toString())
                .withIssuedAt(now)
                .withExpiresAt(now.plus(ttl))
                .sign(algorithm);
    }

    @Override
    public Optional<UUID> verify(String token) {
        try {
            String subject = verifier.verify(token).getSubject();
            return Optional.ofNullable(subject).map(UUID::fromString);
        } catch (JWTVerificationException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}