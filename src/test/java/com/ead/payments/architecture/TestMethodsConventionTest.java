package com.ead.payments.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "com.ead.payments")
public class TestMethodsConventionTest {

    // All the test methods must be named with the following pattern: should[MethodName]When[Scenario]
    @ArchTest
    private static final ArchRule testMethodsShouldBeNamedWithTheAllowedPattern =
            methods()
                    .that().areAnnotatedWith(org.junit.jupiter.api.Test.class)
                    .should().haveNameMatching("should.*When.*")
                    .andShould().beAnnotatedWith(org.junit.jupiter.api.DisplayName.class)
                    .because("Test methods should be named with the allowed pattern");
}
