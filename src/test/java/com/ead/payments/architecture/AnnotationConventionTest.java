package com.ead.payments.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

@AnalyzeClasses(packages = "com.ead.payments", importOptions = ImportOption.DoNotIncludeTests.class)
public class AnnotationConventionTest {

    // This test will verify if classes name with suffix  Controller are annotated with @RestController  or @Controller
    @ArchTest
    private static final ArchRule classesNamedControllerShouldBeAnnotatedWithRestController =
        classes().that().haveSimpleNameEndingWith("Controller")
            .should().beAnnotatedWith(RestController.class)
            .orShould().beAnnotatedWith(Controller.class)
            .because("Classes named with suffix Controller should be annotated with @RestController or @Controller");

    // This test will verify if classes name with suffix  Service are annotated with @Service
    @ArchTest
    private static final ArchRule classesNamedServiceShouldBeAnnotatedWithService =
        classes().that().haveSimpleNameEndingWith("Service")
            .should().beAnnotatedWith(org.springframework.stereotype.Service.class)
            .because("Classes named with suffix Service should be annotated with @Service");

    // This test will verify if classes name with suffix  Repository are annotated with @Repository
    @ArchTest
    private static final ArchRule classesNamedRepositoryShouldBeAnnotatedWithRepository =
        classes().that().haveSimpleNameEndingWith("Repository")
            .should().beAnnotatedWith(org.springframework.stereotype.Repository.class)
            .because("Classes named with suffix Repository should be annotated with @Repository");

    // This test will verify if classes name with suffix  Configuration are annotated with @Configuration
    @ArchTest
    private static final ArchRule classesNamedConfigurationShouldBeAnnotatedWithConfiguration =
        classes().that().haveSimpleNameEndingWith("Configuration")
            .should().beAnnotatedWith(org.springframework.context.annotation.Configuration.class)
            .because("Classes named with suffix Configuration should be annotated with @Configuration");

    // This test will verify if classes name with suffix  Listener are annotated with @Component
    @ArchTest
    private static final ArchRule classesNamedListenerShouldBeAnnotatedWithComponent =
        classes().that().haveSimpleNameEndingWith("Listener")
            .should().beAnnotatedWith(org.springframework.stereotype.Component.class)
            .because("Classes named with suffix Listener should be annotated with @Component");

    // This test will verify if classes name with suffix  Mapper are annotated with @Mapper
    @ArchTest
    private static final ArchRule classesNamedMapperShouldBeAnnotatedWithMapper =
        classes().that().haveSimpleNameEndingWith("Mapper")
            .should().beAnnotatedWith(org.mapstruct.Mapper.class)
            .because("Classes named with suffix Mapper should be annotated with @Mapper")
                .allowEmptyShould(true);

}
