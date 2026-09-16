package com.bcnc.prices.architecture;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Verifica en compilación de tests que la arquitectura hexagonal se respeta de
 * verdad, no solo por convención: domain/ no puede depender de Spring/JPA/Web,
 * y application/ no puede depender de detalles técnicos de infrastructure/.
 */
class HexagonalArchitectureTest {

    private static final String BASE_PACKAGE = "com.bcnc.prices";

    private final com.tngtech.archunit.core.domain.JavaClasses classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(BASE_PACKAGE);

    @Test
    void domainNoDependeDeSpringNiJpaNiWeb() {
        ArchRule rule = noClasses()
                .that().resideInAPackage(BASE_PACKAGE + ".domain..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "org.springframework..",
                        "jakarta.persistence..",
                        "jakarta.servlet..",
                        "jakarta.validation.."
                );

        rule.check(classes);
    }

    @Test
    void applicationNoDependeDeInfrastructure() {
        ArchRule rule = noClasses()
                .that().resideInAPackage(BASE_PACKAGE + ".application..")
                .should().dependOnClassesThat()
                .resideInAPackage(BASE_PACKAGE + ".infrastructure..");

        rule.check(classes);
    }

    @Test
    void soloInfrastructureAccedeAJpaYWeb() {
        ArchRule rule = noClasses()
                .that().resideOutsideOfPackage(BASE_PACKAGE + ".infrastructure..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("jakarta.persistence..", "org.springframework.web..");

        rule.check(classes);
    }

    @Test
    void domainNoDependeDeApplicationNiInfrastructure() {
        ArchRule rule = noClasses()
                .that().resideInAPackage(BASE_PACKAGE + ".domain..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(BASE_PACKAGE + ".application..", BASE_PACKAGE + ".infrastructure..");

        rule.check(classes);
    }
}
