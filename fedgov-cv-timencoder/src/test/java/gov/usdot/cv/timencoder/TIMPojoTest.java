/*
 * Copyright (C) 2025 LEIDOS.
 * Apache License 2.0
 */
package gov.usdot.cv.timencoder;

import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;

import org.junit.Test;

import java.util.List;

/**
 * Sweeps every POJO in gov.usdot.cv.timencoder and validates all
 * getters/setters. Covers the ~1,032 uncovered POJO lines driving
 * the timencoder "20.6% coverage" figure in SonarCloud.
 *
 * DEPENDENCY REQUIRED: add to fedgov-cv-timencoder/pom.xml test scope:
 *   <dependency>
 *     <groupId>com.openpojo</groupId>
 *     <artifactId>openpojo</artifactId>
 *     <version>0.9.1</version>
 *     <scope>test</scope>
 *   </dependency>
 */
public class TIMPojoTest {

    private static final String POJO_PACKAGE = "gov.usdot.cv.timencoder";

    @Test
    public void testGettersAndSetters() {
        Validator validator = ValidatorBuilder.create()
                .with(new SetterTester())
                .with(new GetterTester())
                .build();

        List<PojoClass> pojoClasses = PojoClassFactory.getPojoClasses(POJO_PACKAGE);
        for (PojoClass pojoClass : pojoClasses) {
            validator.validate(pojoClass);
        }
    }
}