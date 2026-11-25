# LostLink — frontend

Frontend aplikacji LostLink zbudowany w React i Vite. Nawigacja działa przez React Router, a żądania `/api` w środowisku developerskim są przekazywane do backendu pod `http://localhost:8080`.

## Uruchomienie

```bash
npm install
npm run dev
```

Przydatne polecenia:

```bash
npm run lint
npm run build
npm run preview
```

## Struktura

```text
src/
  app/          konfiguracja aplikacji, routing i provider sesji
  components/   współdzielone komponenty, np. ikony
  features/     funkcjonalności pogrupowane według domeny
  layouts/      wspólny układ stron
  lib/          integracje i narzędzia infrastrukturalne, np. API
  pages/        strony routowane
  styles/       globalne style
  utils/        współdzielone funkcje pomocnicze
```

Obecne trasy: `/` (ogłoszenia), `/login` (logowanie i rejestracja) oraz `/account` (konto wymagające zalogowania).
