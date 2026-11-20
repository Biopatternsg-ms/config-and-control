# Boundary Specification: Outbound Adapters Isolation

## Overview

This specification establishes the architectural boundary rules for outbound infrastructure adapters in `com.biopatternsg.infrastructure.adapters.out.repositories.*`.

---

## 1. Outbound Ports & Adapter Implementations

| Outbound Adapter Class | Outbound Port Interface (Domain) | External Client Dependency | Permitted Invocations |
| :--- | :--- | :--- | :--- |
| `BiologicalObjectAdapter` | `BiologicalObjectRepository` | `BiologicalObjectHttpClient`, `SessionUtil` | External REST Client |
| `InferencesAdapter` | `TriggerInferences` | `InferencesRestClient`, `SessionUtil` | External REST Client |
| `PubmedIntegrationAdapter` | `TriggerPubmedIntegration` | `PubmedRestClient`, `SessionUtil` | External REST Client |

---

## 2. Inviolable Boundary Rules

1. **Zero Dependencies on Application Services**:
   - Outbound adapters MUST NOT import any class or interface from `com.biopatternsg.application.services..`.
   - Outbound adapters MUST NOT inject `PipelineService` or `PipelineStepOrchestrator`.
2. **Failure Handling**:
   - Outbound adapters must execute external HTTP communications and propagate any communication/runtime errors as unchecked exceptions.
   - The application layer (`PipelineStepOrchestrator`) is solely responsible for recording step failures (`Status.FAILED`) on the domain aggregate.
3. **No Unused Injections**:
   - Adapters must contain zero unused dependencies or injection points.
