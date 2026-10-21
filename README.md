# Legal Support Spring Boot API

Java 21, Spring Boot, PostgreSQL, Flyway and Microsoft Entra ID resource-server backend.

## Run with the Angular Compose stack
Place this repository next to `legal-support-frontend`, populate the frontend `.env`, then run from the frontend folder:

```powershell
docker compose up --build -d
docker compose ps
docker compose logs -f backend
```

## Security
The API validates `iss`, signature, expiry and the expected `aud` claim. Entra app roles are converted to `ROLE_*`. Create the roles `REQUESTER`, `FINANCE`, `PM`, `LEGAL_ADMIN`, `LEGAL_USER`, `HEAD_OF_LEGAL`, `SENIOR_MANAGEMENT`, `SYSTEM_ADMIN` in the backend App Registration.

## Implemented endpoints
- `GET /api/matters`
- `POST /api/matters`
- `POST /api/matters/{ref}/assignments`
- `PATCH /api/matters/{ref}/status`
- `POST /api/matters/{ref}/documents`
- `POST /api/matters/{ref}/feedback`
- `GET /actuator/health/readiness`

Documents use local container storage by default. Implement a `DocumentStorage` bean under a `graph` profile for production SharePoint/Microsoft Graph storage. Do not put Graph client secrets in Angular.
