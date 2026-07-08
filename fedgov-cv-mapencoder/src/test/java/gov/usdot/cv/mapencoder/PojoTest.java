package gov.usdot.cv.mapencoder;

import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;
import org.junit.Test;

import java.util.List;

public class PojoTest {

    private static final String POJO_PACKAGE = "gov.usdot.cv.mapencoder";

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