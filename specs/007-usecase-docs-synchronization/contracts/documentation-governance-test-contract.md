# Contract: DocumentationGovernanceTest

**Package**: `archtest`
**Class**: `DocumentationGovernanceTest`
**Location**: `src/test/java/archtest/DocumentationGovernanceTest.java`

## Specification Contract

```java
package archtest;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DocumentationGovernanceTest {

    private static final String ROOT_USE_CASES = "com.biopatternsg.application.usecase";
    private static final Path USE_CASES_DOC_DIR = Path.of(".agents/use_cases");
    private static final Path ARCHITECTURE_MD = Path.of(".agents/architecture.md");

    @DisplayName("Every concrete UseCase class must have a matching documentation markdown file")
    @Test
    void everyUseCaseMustHaveDocumentationFileTest();

    @DisplayName("Every concrete UseCase must be indexed in .agents/architecture.md")
    @Test
    void everyUseCaseMustBeIndexedInArchitectureDocTest() throws IOException;
}
```

## Invariants
1. Evaluates all top-level classes in `com.biopatternsg.application.usecase..` ending with `UseCase`.
2. Fails the build if any use case class has no corresponding non-empty file `.agents/use_cases/<SimpleName>.md`.
3. Fails the build if `.agents/architecture.md` lacks a link to `.agents/use_cases/<SimpleName>.md`.
