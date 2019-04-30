package uk.gov.hmcts.reform.professionalapi.domain.entities;

import org.junit.Test;
import org.meanbean.factories.FactoryCollection;
import org.meanbean.lang.Factory;
import org.meanbean.test.BeanTester;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class AbstractEntityTest {

    @Test
    public void getterAndSetterCorrectness() throws Exception {
        final BeanTester beanTester = new BeanTester();
        FactoryCollection factoryCollection = beanTester.getFactoryCollection();
        factoryCollection.addFactory(LocalDateTime.class, new LocalDateTimeFactory());
        factoryCollection.addFactory(UUID.class,new UUIDFactory());
        beanTester.testBean(getBeanInstance().getClass());
    }

    protected abstract Object getBeanInstance();

    class LocalDateTimeFactory implements Factory {

        @Override
        public LocalDateTime create() {
            return LocalDateTime.now();
        }
    }

    class UUIDFactory implements Factory {

        @Override
        public UUID create() {
            return UUID.randomUUID();
        }
    }
}
