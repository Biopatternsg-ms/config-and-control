package archtest;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.library.Architectures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

class ArchitectureLayerTest {

    private static final String ROOT = "com.biopatternsg";

    private JavaClasses javaClasses;

    @BeforeEach
    void init() {
        this.javaClasses = new ClassFileImporter().importPackages(ROOT);
    }

    @DisplayName("The class in the layer applications should only used for others class by the same layer")
    @Test
    void layerApplicationTest() {
        Architectures.LayeredArchitecture architecture = layeredArchitecture()
                .consideringAllDependencies()
                .layer("Application").definedBy(ROOT + ".application..")
                .whereLayer("Application").mayOnlyBeAccessedByLayers("Application");
        architecture.check(this.javaClasses);

    }

    @DisplayName("The implementation of the uses cases should not invoked directly")
    @Test
    void layerApplicationUsesCasesTest() {

            Architectures.LayeredArchitecture architecture = layeredArchitecture()
                    .consideringAllDependencies()
                    .layer("ApplicationUsesCases").definedBy(ROOT+".application.usecase..")
                    .whereLayer("ApplicationUsesCases").mayNotBeAccessedByAnyLayer();
            architecture.check(this.javaClasses);

    }

}
