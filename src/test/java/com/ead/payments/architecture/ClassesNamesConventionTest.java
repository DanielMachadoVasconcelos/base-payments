package com.ead.payments.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "com.ead.payments", importOptions = ImportOption.DoNotIncludeTests.class)
class ClassesNamesConventionTest {

    // This test will verify if the classes are named with the allowed suffixes
    @ArchTest
    private static final ArchRule classesShouldBeNamedOnlyWithTheAllowedSuffixes =
        classes().should().haveSimpleNameEndingWith("Application")
            .orShould().haveSimpleNameEndingWith("Handler")
            .orShould().haveSimpleNameEndingWith("Exception")
            .orShould().haveSimpleNameEndingWith("Interceptor")
            .orShould().haveSimpleNameEndingWith("Advice")
            .orShould().haveSimpleNameEndingWith("Aggregate")
            .orShould().haveSimpleNameEndingWith("Order")
            .orShould().haveSimpleNameEndingWith("Product")
            .orShould().haveSimpleNameEndingWith("Controller")
            .orShould().haveSimpleNameEndingWith("Listener")
            .orShould().haveSimpleNameEndingWith("Service")
            .orShould().haveSimpleNameEndingWith("Repository")
            .orShould().haveSimpleNameEndingWith("Configuration")
            .orShould().haveSimpleNameEndingWith("Entity")
            .orShould().haveSimpleNameEndingWith("Status")
            .orShould().haveSimpleNameEndingWith("Event")
            .orShould().haveSimpleNameEndingWith("Command")
            .orShould().haveSimpleNameEndingWith("Mapper")
            .orShould().haveSimpleNameEndingWith("Request")
            .orShould().haveSimpleNameEndingWith("Response")
            .orShould().haveSimpleNameEndingWith("Client")
            .orShould().haveSimpleNameEndingWith("Gateway")
            .orShould().haveSimpleNameEndingWith("Properties")
            .because("Classes should be named with the allowed suffixes");

    // This test will verify if the classes are written in english
    @ArchTest
    private static final ArchRule classesShouldBeWrittenInEnglish =
            classes().should().haveNameMatching("^[\\p{ASCII}]*$")
                    .because("Classes should be named using only English letters, digits, and standard symbols.");

    // This test will verify if the classes are named without underscore
    @ArchTest
    private static final ArchRule classesShouldNotHaveUnderscore =
            classes().should().haveNameNotMatching(".*_.*")
                    .because("Classes should be named without underscore.");
}
