/*
 * Copyright (C) 2025 LEIDOS.
 * Apache License 2.0
 */
package gov.usdot.cv.rgaencoder;

import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;

import org.junit.Test;

import java.util.List;

/**
 * Sweeps every POJO in the gov.usdot.cv.rgaencoder package and verifies
 * that all getters and setters work correctly.
 *
 * This single test covers the ~1,042 uncovered POJO lines that inflate
 * the rgaencoder "1.1% coverage" number in SonarCloud.
 *
 * DEPENDENCY REQUIRED: add to fedgov-cv-rgaencoder/pom.xml test scope:
 *   <dependency>
 *     <groupId>com.openpojo</groupId>
 *     <artifactId>openpojo</artifactId>
 *     <version>0.9.1</version>
 *     <scope>test</scope>
 *   </dependency>
 */
public class RGAPojoTest {

    private static final String POJO_PACKAGE = "gov.usdot.cv.rgaencoder";

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