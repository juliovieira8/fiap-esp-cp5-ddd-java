package br.com.fiap.bank.core.presentation;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Analise estatica do bytecode: complementa o maven-enforcer-plugin garantindo que nenhuma classe
 * da camada presentation referencie diretamente domain ou infrastructure.
 */
class ArquiteturaEmCamadasTest {

    private static final JavaClasses CLASSES_DA_PRESENTATION = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("br.com.fiap.bank.core.presentation");

    @Test
    void presentationNaoDeveAcessarDiretamenteDomainNemInfrastructure() {
        noClasses().that().resideInAPackage("br.com.fiap.bank.core.presentation..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("br.com.fiap.bank.core.domain..", "br.com.fiap.bank.core.infrastructure..")
                .check(CLASSES_DA_PRESENTATION);
    }
}
