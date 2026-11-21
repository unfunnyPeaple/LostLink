# LostLink

Portal rzeczy zagubionych i znalezionych – backend w Javie (serwlety, Tomcat, SQLite), frontend w React.

## 1. Sklonuj repozytorium

```bash
git clone https://github.com/unfunnyPeaple/LostLink.git
cd LostLink
```

## 2. Utwórz plik `Backend/.env`

```env
DB_PATH=data/database.db
APP_PORT=8080
ADMIN_EMAIL=admin@lostlink.local
ADMIN_PASSWORD=haslo-min-12-znakow
JWT_SECRET=dowolny-losowy-ciag-min-32-znaki
JWT_TTL_MINUTES=60
```

## 3. Uruchom backend

### Opcja A – Docker

Wymagany Docker Desktop.

```bash
cd Backend
docker compose up -d --build
```

### Opcja B – bez Dockera (IntelliJ IDEA + Tomcat)

Wymagane: JDK 17+, IntelliJ IDEA Ultimate, rozpakowany [Apache Tomcat 10.1](https://tomcat.apache.org/download-10.cgi).

1. Otwórz folder `Backend` w IntelliJ (projekt Maven).
2. **Run → Edit Configurations → + → Tomcat Server → Local** i wskaż katalog Tomcata, port HTTP `8080`.
3. Zakładka **Deployment → + → Artifact → `ti:war exploded`**, Application context: `/`.
4. Kliknij **Run**.

W obu przypadkach backend działa pod `http://localhost:8080`.

## 4. Uruchom frontend

Wymagany Node.js 20.19+. W drugim terminalu:

```bash
cd Frontend
npm install
npm run dev
```

Otwórz `http://localhost:5173` i zaloguj się danymi `ADMIN_EMAIL` / `ADMIN_PASSWORD` z pliku `.env`.
