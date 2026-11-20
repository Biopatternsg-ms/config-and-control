# BioPatternsG — `config-and-control`

Core configuration, control, security, and biological pipeline orchestration microservice within the **BioPatternsG** ecosystem.

Built with **Java 21** and **Quarkus 3**, strictly adhering to **Hexagonal Architecture (Ports & Adapters)** and **Clean Architecture** principles.

---

## 1. Overview

The `config-and-control` service acts as the central coordinator between the user interface (`biopatternsg-ui`), the identity provider (**Keycloak**), the document database (**MongoDB**), and the scientific processing microservices (`biological-objects`, `pubmed-integration`, `inferences`).

### Key Responsibilities
- **Biological Network Management (`Networks`):** Creation, updates, and paginated querying of user-owned biological networks.
- **Pipeline Configuration & Lifecycle (`Pipelines`):** Management of search parameters, transcription factor configurations, expert objects, hierarchy levels, and manual confirmation of aligned biological objects.
- **Execution Orchestration & Monitoring:** Launching experiments, reactive phase transitions (`PipelineSteps`), execution timeline/metrics calculation, and automated batch self-healing of in-progress pipelines (`resume`).
- **User Authentication & Administration (`Users` / `Admin`):** OIDC login, token refresh, researcher registration, email verification via Keycloak webhooks, password recovery, and user synchronization.

---

## 2. Architecture & Use Cases Documentation

Comprehensive technical documentation, layer diagrams, sequence flows, and development standards for this service are maintained in the [`.agents/`](.agents/) directory:

- 🏛️ [**Service Architecture (`.agents/architecture.md`)**](.agents/architecture.md): Hexagonal Architecture layer diagram, package structure, MongoDB collections, and external service integrations.
- 📚 [**Use Cases Catalog (`.agents/use_cases/README.md`)**](.agents/use_cases/README.md): Complete index linking to the individual documentation and **Mermaid** sequence diagrams for all 13 use cases in the service.
- 📐 [**Architectural Rules (`.agents/architecture-rules.md`)**](.agents/architecture-rules.md): Mandatory layer isolation rules, use case design guidelines, application service encapsulation, and documentation governance.
- ☕ [**Java & Clean Code Rules (`.agents/java-rules.md`)**](.agents/java-rules.md): Java 21 coding standards, stream/lambda purity rules, method parameter limits, and static mapper conventions.
- 📜 [**Project Constitution (`.specify/memory/constitution.md`)**](.specify/memory/constitution.md): Core governing principles for Spec-Kit workflows and AI agents.

---

## 3. Environment Variables

To run the service locally or in a containerized environment, configure the following environment variables (mapped in [`src/main/resources/application.properties`](src/main/resources/application.properties)):

### 3.1. HTTP Server & Database (MongoDB)

| Variable | Description | Default Value | Required |
|---|---|---|---|
| `PORT` | HTTP port on which the microservice listens. | `8080` | No |
| `DB_URL` | MongoDB connection string. | `mongodb://localhost:27017` | No |
| `DB_USER` | MongoDB authentication username (`auth-source=admin`). | — | **Yes** |
| `DB_PASSWORD` | MongoDB authentication password. | — | **Yes** |
| `DB_NAME` | Target MongoDB database name. | — | **Yes** |

### 3.2. Identity Provider (Keycloak)

| Variable | Description | Default Value | Required |
|---|---|---|---|
| `KC_URL` | Base URL of the Keycloak server (`keycloak-auth-api` REST client). | — | **Yes** |
| `KC_CLIENT_ID` | OIDC Client ID configured in the Keycloak realm. | — | **Yes** |
| `KC_CLIENT_SECRET` | OIDC Client Secret configured in Keycloak. | — | **Yes** |
| `KC_GRANT_TYPE_USER` | OAuth2/OIDC grant type for user authentication (e.g., `password`). | — | **Yes** |
| `KC_GRANT_TYPE_CLIENT` | OAuth2/OIDC grant type for admin client operations (e.g., `client_credentials`). | — | **Yes** |
| `KC_SCOPE` | OAuth2/OIDC scopes requested during authentication (e.g., `openid`). | — | **Yes** |
| `KC_REDIRECT_URI` | Redirect URI used in Keycloak action emails (email verification / password reset). | `https://bioai.redclara.net` | No |

### 3.3. Internal Microservices Integration

| Variable | Description | Default Value | Required |
|---|---|---|---|
| `BIOLOGICAL_OBJECTS` | Base URL for the `search-biological-objects` service. | `http://biological-objects:8080` | No |
| `PUBMED_INTEGRATION_API` | Base URL for the `pubmed-integration` service. | `http://pubmed-integration:8080` | No |
| `INFERENCES_URL` | Base URL for the `inferences` service. | `http://inferences:8080` | No |

---

## 4. Building, Testing, and Running

### 4.1. Prerequisites
- **JDK 21+**
- **Maven 3.9+** (or use the included `./mvnw` wrapper)
- Reachable **MongoDB** and **Keycloak** instances (with the environment variables configured above)

### 4.2. Running in Development Mode (Live Coding)
```bash
./mvnw quarkus:dev
```
> Quarkus Dev UI is available in dev mode at `http://localhost:8080/q/dev/` and Swagger UI at `http://localhost:8080/q/swagger-ui/`.

### 4.3. Running Tests & Architectural Verification
```bash
# Run the full test suite (Unit Tests + ArchUnit + Documentation Governance)
./mvnw test

# Run Hexagonal Architecture layer isolation tests only
./mvnw test -Dtest=ArchitectureLayerTest

# Run Use Case documentation synchronization tests only
./mvnw test -Dtest=DocumentationGovernanceTest
```

### 4.4. Packaging for Production
```bash
# Package the application (Quarkus fast-jar)
./mvnw package

# Run the packaged application
java -jar target/quarkus-app/quarkus-run.jar
```
