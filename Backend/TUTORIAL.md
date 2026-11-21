# LostLink – Backend

Tomcat 10 + Servlety (bez frameworka), SQLite, JWT.

```powershell
docker compose up -d --build
docker compose logs -f app
```

## Struktura

```
domain/
  exception/   wyjątki domenowe → kody HTTP
  model/       rekordy i enumy (User, UserRole)
  port/        INTERFEJSY (UserRepository, PasswordHasher, TokenService)
application/
  dto/         dane zwracane na zewnątrz (UserDto)
  *Service     logika biznesowa (AuthService, UserService)
infrastructure/
  config/      AppConfig (.env), AppListener (składanie aplikacji)
  persistence/ Jdbc + implementacje repozytoriów (SqliteUserRepository)
  security/    implementacje portów (Pbkdf2PasswordHasher, JwtTokenService)
  web/         ApiServlet, BearerAuth + kontrolery (AuthServlet, UsersServlet)
resources/db/schema.sql
```

**Kontroler** – wszystko, co HTTP: czyta body, weryfikuje token, wywołuje serwis, odsyła JSON.
**Serwis** – logika, walidacja, uprawnienia. Dostaje dane jako argumenty, zwraca DTO. Nie zna HTTP.
**Repozytorium** – tylko SQL.

---

## Nowa funkcjonalność krok po kroku

Każdy krok pokazany na tym, jak zrobiony jest moduł użytkowników.

### 1. Tabela – `resources/db/schema.sql`

```sql
CREATE TABLE IF NOT EXISTS users (
    id            TEXT PRIMARY KEY,
    email         TEXT NOT NULL UNIQUE COLLATE NOCASE,
    ...
    user_role     TEXT NOT NULL DEFAULT 'USER' CHECK (user_role IN ('ADMIN', 'USER')),
    is_blocked    INTEGER NOT NULL DEFAULT 0 CHECK (is_blocked IN (0, 1)),
    created_at    TEXT NOT NULL
);
```

- zawsze `IF NOT EXISTS`, każda instrukcja kończy się `;`
- SQLite nie ma UUID / enum / boolean / daty: `UUID` → `TEXT`, enum → `TEXT` + `CHECK`, `boolean` → `INTEGER` 0/1, `Instant` → `TEXT`
- zmiana istniejącej tabeli w trakcie developmentu: usuń plik bazy z `data/` i uruchom ponownie

### 2. Model – `domain/model`

```java
public enum UserRole { ADMIN, USER }

public record User(UUID id, String email, String passwordHash, String displayName,
                   UserRole role, boolean blocked, Instant createdAt) {}
```

Typy Javy i camelCase – tłumaczenie na kolumny robi repozytorium.

### 3. Interfejs repozytorium – `domain/port`

**Najpierw interfejs, implementacja dopiero w kroku 6.**

```java
public interface UserRepository {
    List<User> findAll();
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);
    boolean existsByRole(UserRole role);

    void save(User user);
    void setBlocked(UUID id, boolean blocked);
    void setRole(UUID id, UserRole role);
    void delete(UUID id);
}
```

Najpierw odczyty, potem zapisy. Jedna metoda = jedna zmiana (`setBlocked`, `setRole`), zamiast ogólnego `update`.
Inne zależności zewnętrzne też jako porty: `PasswordHasher`, `TokenService`.

### 4. DTO – `application/dto`

To, co zobaczy klient. Bez wrażliwych pól, proste typy.

```java
public record UserDto(String id, String email, String displayName,
                      String role, boolean blocked, String createdAt) {
    public static UserDto from(User user) { ... }
}
```

`UserDto` nie ma `passwordHash`. `createdAt` jako `String`, bo Jackson nie zapisze `Instant` bez dodatkowego modułu.

### 5. Serwis – `application`

Zależy **tylko od interfejsów**. Waliduje, sprawdza uprawnienia, rzuca wyjątki domenowe, loguje zdarzenia, zwraca DTO.

```java
public class UserService {
    private static final Logger LOG = Logger.getLogger(UserService.class.getName());
    private final UserRepository userRepository;

    public UserDto setBlocked(UUID currentUserId, UUID userId, boolean blocked) {
        requireAdmin(currentUserId);                         // 403
        requireNotSelf(currentUserId, userId, "zablokować");  // 403
        requireExists(userId);                               // 404

        userRepository.setBlocked(userId, blocked);
        LOG.info("Admin " + currentUserId + " zablokował użytkownika " + userId);
        return UserDto.from(findById(userId));
    }
}
```

- schemat każdej metody: **sprawdzenia → jedna zmiana → log → DTO**
- kto wykonuje akcję: `UUID currentUserId` jako pierwszy argument; serwis sam sprawdza, czy konto istnieje, nie jest zablokowane i ma uprawnienia (`loadActiveUser`, `requireAdmin` w `UserService`)
- id i data tworzone w serwisie (`UUID.randomUUID()`, `Instant.now()`) – patrz `AuthService.createUser`
- nigdy nie loguj haseł ani tokenów

### 6. Implementacja repozytorium – `infrastructure/persistence`

Implementuje interfejs z kroku 3 przez `Jdbc`.

```java
public class SqliteUserRepository implements UserRepository {
    private static final String COLUMNS =
            "id, email, password_hash, display_name, user_role, is_blocked, created_at";
    private final Jdbc jdbc;

    @Override
    public Optional<User> findById(UUID id) {
        return jdbc.queryOne("SELECT " + COLUMNS + " FROM users WHERE id = ?", this::map, id.toString());
    }

    @Override
    public void setBlocked(UUID id, boolean blocked) {
        jdbc.update("UPDATE users SET is_blocked = ? WHERE id = ?", blocked ? 1 : 0, id.toString());
    }

    private User map(ResultSet rs) throws SQLException { ... }   // wiersz → model
}
```

| `Jdbc` | Zwraca | Do czego |
|---|---|---|
| `query(sql, mapper, params...)` | `List<T>` | SELECT wielu |
| `queryOne(sql, mapper, params...)` | `Optional<T>` | SELECT jednego |
| `update(sql, params...)` | `int` | INSERT z własnym id, UPDATE, DELETE |
| `insert(sql, params...)` | `long` | INSERT z id generowanym przez bazę |

Parametry zawsze przez `?`. Bez `try/catch` – `Jdbc` sam zamyka zasoby.

### 7. Kontroler – `infrastructure/web`

```java
public class UsersServlet extends ApiServlet {

    public record BlockRequest(UUID id, Boolean blocked) {}

    private final BearerAuth auth;
    private final UserService userService;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        switch (route(req)) {
            case "/block"  -> setBlocked(req, resp);
            case "/role"   -> changeRole(req, resp);
            case "/delete" -> delete(req, resp);
            default        -> throw unknownEndpoint(req);
        }
    }

    /** POST /api/users/block (admin) */
    private void setBlocked(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UUID currentUserId = auth.requireUserId(req);
        BlockRequest body = body(req, BlockRequest.class);
        UUID userId = required(body.id(), "id");
        boolean blocked = required(body.blocked(), "blocked");

        UserDto user = userService.setBlocked(currentUserId, userId, blocked);

        ok(resp, user);
    }
}
```

- `doGet` / `doPost` = **tylko routing**: jawne ścieżki w `case`, `default` → `unknownEndpoint`
- każda akcja = **3 bloki**: HTTP → dane, wywołanie serwisu, dane → HTTP
- Javadoc nad akcją: metoda + pełna ścieżka
- body = rekord w kontrolerze tylko z polami, które klient może wysłać; pola wymagane jako `Boolean`/`UUID` (obiekty), żeby brak dał `null` → `required` → 400
- bez `@WebServlet` i bez `try/catch`

**Narzędzia z `ApiServlet`:** `route`, `body`, `required`, `unknownEndpoint`, `ok`, `created`, `noContent`.
**Token:** `auth.requireUserId(req)` (`BearerAuth`) → `UUID` albo 401.

### 8. Rejestracja – `infrastructure/config/AppListener`

```java
// Implementacje portów
UserRepository userRepository = new SqliteUserRepository(jdbc);

// Serwisy
UserService userService = new UserService(userRepository);

// Kontrolery
mount(ctx, "/api/users/*", new UsersServlet(bearerAuth, userService));
```

Zmienna ma typ **interfejsu**. `/*` na końcu – jeden servlet obsługuje wszystkie podścieżki.
Obiekty tworzy się **tylko tutaj**.

### 9. Build i sprawdzenie

```powershell
docker compose up -d --build
docker compose logs app --tail 30      # brak SEVERE
```

---

## Nowe narzędzie

Najpierw zdecyduj, **kto będzie go używał**:

| Używa go | Gdzie | Interfejs? | Przykład |
|---|---|---|---|
| serwis (logika biznesowa) | interfejs `domain/port` + implementacja w `infrastructure/...` | **tak** | `PasswordHasher`, `TokenService` |
| tylko kontrolery (HTTP) | `infrastructure/web` | nie | `BearerAuth`, `Json`, metody w `ApiServlet` |
| tylko repozytoria (SQL) | `infrastructure/persistence` | nie | `Jdbc` |

Serwis nie może zależeć od klas z `infrastructure`, dlatego narzędzie dla serwisu zawsze dostaje interfejs (port).

### Narzędzie dla serwisu – krok po kroku

Na przykładzie `TokenService` (wydawanie i weryfikacja JWT).

**1. Interfejs w `domain/port`** – co narzędzie robi, bez szczegółów technicznych:

```java
public interface TokenService {
    /** Wydaje token dla użytkownika. */
    String issue(UUID userId);

    /** Id użytkownika z poprawnego, niewygasłego tokenu; pusty, gdy token jest zły. */
    Optional<UUID> verify(String token);
}
```

W nazwach i typach nie ma słowa „JWT” ani klas z bibliotek – tylko typy z JDK i domeny.

**2. Implementacja w `infrastructure/...`** – tu jest biblioteka i technika:

```java
// infrastructure/security/JwtTokenService.java
public final class JwtTokenService implements TokenService {

    public JwtTokenService(String secret, Duration ttl) { ... }

    @Override
    public String issue(UUID userId) { ... }          // com.auth0.jwt

    @Override
    public Optional<UUID> verify(String token) { ... }
}
```

Podpakiet według rodzaju: `security/` (hasła, tokeny), `persistence/` (baza), nowy podpakiet dla innych (np. `mail/`).
Biblioteka (jeśli potrzebna) → `pom.xml`. Ustawienia (sekret, czas) → konstruktor, nie `AppConfig.load()` w środku.

**3. Konfiguracja (jeśli potrzebna)** – zmienna w `.env` + pole w `AppConfig` (patrz niżej):

```java
public record AppConfig(..., String jwtSecret, Duration jwtTtl, ...) { ... }
```

**4. Utworzenie w `AppListener`** – zmienna z typem **interfejsu**:

```java
// Implementacje portów
TokenService tokenService = new JwtTokenService(config.jwtSecret(), config.jwtTtl());
```

**5. Wstrzyknięcie przez konstruktor** tam, gdzie jest potrzebne:

```java
AuthService authService = new AuthService(userRepository, passwordHasher, tokenService);
BearerAuth bearerAuth = new BearerAuth(tokenService);
```

```java
public class AuthService {
    private final TokenService tokenService;      // interfejs, nie JwtTokenService

    public String login(String email, String password) {
        ...
        return tokenService.issue(user.id());
    }
}
```

Wymiana implementacji (np. JWT → sesje w bazie) = nowa klasa w `infrastructure` + zmiana jednej linijki w `AppListener`. Serwisy zostają bez zmian.

---

## Wyjątki

Rzucasz, nie łapiesz. `ApiServlet.service()` owija każde żądanie w `try` – wyjątek rzucony w kontrolerze, serwisie albo repozytorium leci w górę i trafia do odpowiedniego `catch`, który odsyła JSON `{"error": "..."}` z kodem HTTP.

| Wyjątek | Gdzie leży | Kto rzuca | Kod |
|---|---|---|---|
| `ValidationException` | `domain/exception` | serwis | 400 |
| `UnauthorizedException` | `domain/exception` | serwis | 401 |
| `ForbiddenException` | `domain/exception` | serwis | 403 |
| `NotFoundException` | `domain/exception` | serwis | 404 |
| `ConflictException` | `domain/exception` | serwis | 409 |
| `HttpError.badRequest/unauthorized/...` | `infrastructure/web` | tylko kontroler / web | 400/401/... |
| `PersistenceException` | `infrastructure/persistence` | `Jdbc` | 500 |
| każdy inny | – | – | 500 + stack trace w logach |

Serwis rzuca **tylko wyjątki domenowe** – nie zna kodów HTTP. Kod przypisuje im dopiero `ApiServlet`.

### Nowy wyjątek domenowy – krok po kroku

Na przykładzie `ConflictException` (zajęty e-mail → 409).

**1. Klasa w `domain/exception`**

```java
package pk.wieik.ti.domain.exception;

/** Operacja koliduje z istniejącym stanem, np. zajęty e-mail (→ 409). */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) { super(message); }
}
```

Zawsze `extends RuntimeException` – wtedy nie trzeba pisać `throws` w metodach. W Javadoc: kiedy występuje i jaki ma kod.

**2. Rejestracja w `infrastructure/web/ApiServlet.service()`**

Dopisz `catch` obok pozostałych, **przed** `catch (Exception e)` (ten musi być ostatni):

```java
        } catch (NotFoundException e) {
            error(req, resp, 404, e.getMessage());
        } catch (ConflictException e) {              // ← nowy
            error(req, resp, 409, e.getMessage());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, ...);
            error(req, resp, 500, "Błąd serwera");
        }
```

i import `pk.wieik.ti.domain.exception.ConflictException`.
Bez tego kroku wyjątek trafi do `catch (Exception e)` i klient dostanie 500.

**3. Rzucanie w serwisie**

```java
if (userRepository.findByEmail(validEmail).isPresent()) {
    throw new ConflictException("Ten e-mail jest już zajęty");
}
```

Komunikat trafia do klienta jako `{"error": "..."}` i do logu żądania – pisz go tak, żeby był zrozumiały dla użytkownika i nie zdradzał szczegółów technicznych.

**4. Dopisz wiersz do tabeli powyżej.**

### Błąd HTTP w kontrolerze

Gdy problem dotyczy samego żądania (brak pola, zły JSON, brak tokenu), nie twórz wyjątku domenowego – użyj gotowego `HttpError` z warstwy web:

```java
if (header == null) {
    throw HttpError.unauthorized("Brak tokenu");     // BearerAuth
}
```

Najczęściej robią to za Ciebie `body(...)`, `required(...)`, `auth.requireUserId(...)` i `unknownEndpoint(...)`.

## Nowa zmienna w `.env`

Pole w rekordzie `AppConfig` + linijka w `load()` (`required("X")` albo `intOr("X", 10)`), przekazanie w `AppListener` do konstruktora.

---

## Checklista

- [ ] tabela w `schema.sql`
- [ ] model w `domain/model`
- [ ] **interfejs w `domain/port`**
- [ ] DTO w `application/dto`
- [ ] serwis w `application`
- [ ] `Sqlite...Repository` w `infrastructure/persistence`
- [ ] kontroler w `infrastructure/web`
- [ ] `mount(...)` w `AppListener`
- [ ] build + test

## Testy

```powershell
powershell -ExecutionPolicy Bypass -File tests\user-test.ps1
```

Dla nowej funkcjonalności poproś AI o wygenerowanie takiego samego pliku jak `tests/user-test.ps1`, np.:

> Na wzór `tests/user-test.ps1` wygeneruj `tests/<nazwa>-test.ps1` dla funkcjonalności `<nazwa>`. Endpointy: `...`. Reguły: `...`.
