# Use Cases Catalog (`config-and-control`)

> **Service:** `config-and-control`  
> **Base Package:** `com.biopatternsg.application.usecase`  
> **Inbound Ports:** `com.biopatternsg.domain.port.in`  

This directory contains the detailed technical and functional documentation for all use cases implemented in the `config-and-control` microservice. Each document describes the business purpose of the use case, its inbound and outbound port contracts, the application services and repositories involved, the step-by-step execution flow, error scenarios, and its **Mermaid** sequence diagram.

---

## Use Cases Index

### 1. Biological Network Management (`Network`)

| Use Case | Inbound Port | Description | Documentation |
|---|---|---|---|
| `CreateNetworkUseCase` | `CreateNetwork` | Registers a new biological network validating name uniqueness per user. | [`CreateNetworkUseCase.md`](CreateNetworkUseCase.md) |
| `UpdateNetworkUseCase` | `UpdateNetwork` | Updates the name and description of an existing biological network. | [`UpdateNetworkUseCase.md`](UpdateNetworkUseCase.md) |
| `FindNetworkUseCase` | `FindNetwork` | Queries biological networks by unique ID, exact name, or paginated filters. | [`FindNetworkUseCase.md`](FindNetworkUseCase.md) |

### 2. Pipeline Configuration, Execution & Orchestration (`Pipeline`)

| Use Case | Inbound Port | Description | Documentation |
|---|---|---|---|
| `CreatePipelineUseCase` | `CreatePipeline` | Creates a pipeline configuration linked to a network and initializes all step statuses. | [`CreatePipelineUseCase.md`](CreatePipelineUseCase.md) |
| `UpdatePipelineUseCase` | `UpdatePipeline` | Modularly updates pipeline sections (description, transcription factor, expert objects, search config, aligned objects). | [`UpdatePipelineUseCase.md`](UpdatePipelineUseCase.md) |
| `UpdatePipelineStepUseCase` | `UpdatePipelineStep` | Updates the status and metrics of a pipeline step and triggers orchestration of the next phase upon completion. | [`UpdatePipelineStepUseCase.md`](UpdatePipelineStepUseCase.md) |
| `FindPipelineUseCase` | `FindPipeline` | Queries pipeline configurations by unique ID, exact name, or paginated filters. | [`FindPipelineUseCase.md`](FindPipelineUseCase.md) |
| `LaunchPipelineUseCase` | `LaunchPipeline` | Initiates execution of a configured pipeline by triggering the `biological-objects` service. | [`LaunchPipelineUseCase.md`](LaunchPipelineUseCase.md) |
| `GetPipelineExecutionUseCase` | `GetPipelineExecution` | Reconstructs the consolidated execution state, step durations, overall progress, and detailed experiment metrics. | [`GetPipelineExecutionUseCase.md`](GetPipelineExecutionUseCase.md) |
| `RegenerateAlignedObjectsUseCase` | `RegenerateAlignedObjects` | Resets the manual alignment step to `PENDING` and re-triggers aligned object generation in `pubmed-integration`. | [`RegenerateAlignedObjectsUseCase.md`](RegenerateAlignedObjectsUseCase.md) |
| `ResumePipelineUseCase` | `ResumePipeline` | Batch-resumes pipelines left in `IN_PROGRESS` status for a target microservice (`SEARCH_BIOLOGICAL_OBJECTS` or `PUBMED_INTEGRATION`). | [`ResumePipelineUseCase.md`](ResumePipelineUseCase.md) |

### 3. User Authentication & Administration (`User` / `Keycloak`)

| Use Case | Inbound Port | Description | Documentation |
|---|---|---|---|
| `UserAuthenticationUseCase` | `UserAuthentication` | Handles user login (`login`) and token renewal (`refreshToken`) against Keycloak. | [`UserAuthenticationUseCase.md`](UserAuthenticationUseCase.md) |
| `UserManagementUseCase` | `UserManagement` | Orchestrates user registration, email verification, password recovery, status updates, and synchronization between Keycloak and MongoDB. | [`UserManagementUseCase.md`](UserManagementUseCase.md) |

---

## Maintenance and Synchronization Rules

In accordance with [`.agents/architecture-rules.md`](../architecture-rules.md) (Section 2.3) and the Project Constitution ([`.specify/memory/constitution.md`](../../.specify/memory/constitution.md) — Principle IV):

1. **Language Standard:** All use case documentation files in `.agents/use_cases/` (including this `README.md`) **MUST be written in English**.
2. **Creating a New Use Case:** Whenever a new use case is added to `com.biopatternsg.application.usecase`, a corresponding `<UseCaseName>.md` file **MUST** be created in this directory (in English) and **referenced in the tables of this `README.md`** (as well as in `.agents/architecture.md`).
3. **Modifying an Existing Use Case:** Whenever the execution flow, contracts, or business rules of an existing use case change, its `<UseCaseName>.md` file and its summary in this `README.md` **MUST** be updated synchronously.
4. **Automated Verification:** Run `./mvnw test -Dtest=DocumentationGovernanceTest` to verify that every use case has a matching `.md` documentation file and is indexed in both `.agents/use_cases/README.md` and `.agents/architecture.md`.
