/*
 * Copyright © 2026 biopatternsg (biopatternsg@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentationGovernanceTest {

    private static final String ROOT_USE_CASES = "com.biopatternsg.application.usecase";
    private static final Path USE_CASES_DOC_DIR = Path.of(".agents/use_cases");
    private static final Path USE_CASES_README_MD = Path.of(".agents/use_cases/README.md");
    private static final Path ARCHITECTURE_MD = Path.of(".agents/architecture.md");

    @DisplayName("Every concrete UseCase class must have a matching documentation markdown file")
    @Test
    void everyUseCaseMustHaveDocumentationFileTest() {
        JavaClasses classes = new ClassFileImporter().importPackages(ROOT_USE_CASES);
        List<JavaClass> useCases = classes.stream()
                .filter(JavaClass::isTopLevelClass)
                .filter(c -> c.getSimpleName().endsWith("UseCase"))
                .toList();

        assertFalse(useCases.isEmpty(), "No UseCase classes discovered");

        for (JavaClass uc : useCases) {
            String fileName = uc.getSimpleName() + ".md";
            Path docPath = USE_CASES_DOC_DIR.resolve(fileName);
            assertTrue(Files.exists(docPath),
                    "Missing use case documentation: " + docPath + " for " + uc.getName());
            assertTrue(docPath.toFile().length() > 0,
                    "Documentation file is empty: " + docPath);
        }
    }

    @DisplayName("Every concrete UseCase must be indexed in .agents/architecture.md")
    @Test
    void everyUseCaseMustBeIndexedInArchitectureDocTest() throws IOException {
        assertTrue(Files.exists(ARCHITECTURE_MD), ".agents/architecture.md must exist");
        String content = Files.readString(ARCHITECTURE_MD);

        JavaClasses classes = new ClassFileImporter().importPackages(ROOT_USE_CASES);
        List<JavaClass> useCases = classes.stream()
                .filter(JavaClass::isTopLevelClass)
                .filter(c -> c.getSimpleName().endsWith("UseCase"))
                .toList();

        assertFalse(useCases.isEmpty(), "No UseCase classes discovered");

        for (JavaClass uc : useCases) {
            assertTrue(content.contains(uc.getSimpleName()),
                    "Architecture doc missing reference to " + uc.getSimpleName());
            assertTrue(content.contains(uc.getSimpleName() + ".md"),
                    "Architecture doc missing link to " + uc.getSimpleName() + ".md");
        }
    }

    @DisplayName("Every concrete UseCase must be indexed in .agents/use_cases/README.md")
    @Test
    void everyUseCaseMustBeIndexedInUseCasesReadmeTest() throws IOException {
        assertTrue(Files.exists(USE_CASES_README_MD), ".agents/use_cases/README.md must exist");
        String content = Files.readString(USE_CASES_README_MD);

        JavaClasses classes = new ClassFileImporter().importPackages(ROOT_USE_CASES);
        List<JavaClass> useCases = classes.stream()
                .filter(JavaClass::isTopLevelClass)
                .filter(c -> c.getSimpleName().endsWith("UseCase"))
                .toList();

        assertFalse(useCases.isEmpty(), "No UseCase classes discovered");

        for (JavaClass uc : useCases) {
            assertTrue(content.contains(uc.getSimpleName()),
                    "use_cases/README.md missing reference to " + uc.getSimpleName());
            assertTrue(content.contains(uc.getSimpleName() + ".md"),
                    "use_cases/README.md missing link to " + uc.getSimpleName() + ".md");
        }
    }
}
