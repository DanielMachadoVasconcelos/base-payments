package com.ead.payments.architecture;


import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

@AnalyzeClasses(packages = "com.ead.payments", importOptions = ImportOption.DoNotIncludeTests.class)
public class LayerConventionTest {

    // This test will verify if the classes with annotation @Service are not in a package named service
    @ArchTest
    private static final ArchRule classesWithServiceAnnotationShouldNotBeInServicePackage =
        classes().that().areAnnotatedWith(Service.class)
            .should().resideOutsideOfPackage("..service..")
            .because("Classes with annotation @Service should not be in a package named service");

    // This test will verify if the classes with annotation @Repository are not in a package named repository
    @ArchTest
    private static final ArchRule classesWithRepositoryAnnotationShouldNotBeInRepositoryPackage =
        classes().that().areAnnotatedWith(Repository.class)
            .should().resideOutsideOfPackage("..repository..")
            .because("Classes with annotation @Repository should not be in a package named repository");

    // This test will verify if the classes with annotation @Controller are not in a package named controller
    @ArchTest
    private static final ArchRule classesWithControllerAnnotationShouldNotBeInControllerPackage =
        classes().that().areAnnotatedWith(RestController.class)
            .should().resideOutsideOfPackage("..controller..")
            .because("Classes with annotation @Controller should not be in a package named controller");

    // This test will verify if the classes with annotation @Service do not depend on classes with annotation @RestController or @Controller
    @ArchTest
    private static final ArchRule classesWithServiceAnnotationShouldNotDependOnClassesWithControllerAnnotation =
        classes().that().areAnnotatedWith(Service.class)
            .should().dependOnClassesThat().areNotAnnotatedWith(RestController.class)
            .andShould().dependOnClassesThat().areNotAnnotatedWith(Controller.class)
            .because("Classes with annotation @Service should not depend on classes with annotation @RestController or @Controller");

    // This test will verify if the classes with annotation @Repository do not depend on classes with annotation @RestController or @Controller or @Service
    @ArchTest
    private static final ArchRule classesWithRepositoryAnnotationShouldNotDependOnClassesWithControllerAnnotation =
        classes().that().areAnnotatedWith(Repository.class)
            .should().dependOnClassesThat().areNotAnnotatedWith(RestController.class)
            .andShould().dependOnClassesThat().areNotAnnotatedWith(Controller.class)
            .andShould().dependOnClassesThat().areNotAnnotatedWith(Service.class)
            .because("Classes with annotation @Repository should not depend on classes with annotation @RestController or @Controller or @Service");

    // This test will verify if the classes with annotation @Component AND have name ending with Listener do not depend on classes with annotation @Repository
    @ArchTest
    private static final ArchRule classesWithComponentAnnotationAndNameEndingWithListenerShouldNotDependOnClassesWithRepositoryAnnotation =
        classes().that().areAnnotatedWith(Component.class)
            .and().haveSimpleNameEndingWith("Listener")
            .should().dependOnClassesThat().areNotAnnotatedWith(Repository.class)
            .because("Classes with annotation @Component AND have name ending with Listener should not depend on classes with annotation @Repository");
}
