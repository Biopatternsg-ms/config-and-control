# Contract: Use Cases Documentation Standard

**Target Directory**: `.agents/use_cases/`
**Target Master Reference**: `.agents/architecture.md`

## Invariants

1. **Parity**: Exactly one `<UseCaseName>.md` file exists in `.agents/use_cases/` for every concrete class in `com.biopatternsg.application.usecase..` ending with `UseCase`.
2. **Metadata Header**: Every `<UseCaseName>.md` file declares:
   - Full Java class name
   - Inbound port interface
   - Outbound ports / application services
   - Invoking REST controller
3. **5-Part Structure**: Every `<UseCaseName>.md` file contains sections 1 to 4 (Propósito, Flujo de Ejecución Paso a Paso, Diagrama de Flujo Mermaid, Catálogo de Errores).
4. **Mermaid Sequence Diagram**: Every file contains a valid ````mermaid sequenceDiagram```` that compiles cleanly without syntax errors and mirrors actual delivery routes and status codes.
5. **Catalog Index**: Section 5 of `.agents/architecture.md` contains a markdown table row for each of the 13 Use Cases, linking to `use_cases/<UseCaseName>.md`.
6. **Encoding**: All files under `.agents/` are encoded in clean UTF-8 without mojibake.
