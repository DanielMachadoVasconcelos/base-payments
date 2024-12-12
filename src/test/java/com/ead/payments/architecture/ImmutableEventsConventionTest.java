package com.ead.payments.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "com.ead.payments", importOptions = ImportOption.DoNotIncludeTests.class)
public class ImmutableEventsConventionTest {

    @ArchTest
    static final ArchRule eventClassesShouldBePublicAndFinal = classes()
            .that().haveSimpleNameEndingWith("Event")
            .should().bePublic()
            .andShould().beTopLevelClasses()
            .because("Event classes should be public and final");

    @ArchTest
    static final ArchRule eventFieldsShouldBePrivateAndFinal = fields()
            .that().areDeclaredInClassesThat().haveSimpleNameEndingWith("Event")
            .should().bePrivate()
            .andShould().beFinal()
            .because("Event fields should be private and final");

    @ArchTest
    static final ArchRule eventClassesShouldNotHaveSetters = noMethods()
            .that().areDeclaredInClassesThat().haveSimpleNameEndingWith("Event")
            .should().haveNameMatching("set.*")
            .because("Event classes should not have setters");

}
