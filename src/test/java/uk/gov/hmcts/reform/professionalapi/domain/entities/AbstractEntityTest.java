package uk.gov.hmcts.reform.professionalapi.domain.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.Test;
import org.meanbean.factories.FactoryCollection;
import org.meanbean.lang.Factory;
import org.meanbean.test.BeanTester;

public abstract class AbstractEntityTest {

    @Test
    public void getterAndSetterCorrectness() throws Exception {
        final BeanTester beanTester = new BeanTester();
        FactoryCollection factoryCollection = beanTester.getFactoryCollection();
        factoryCollection.addFactory(LocalDateTime.class, new LocalDateTimeFactory());
        factoryCollection.addFactory(UUID.class,new UuidFactory());
        beanTester.testBean(getBeanInstance().getClass());
    }

    protected abstract Object getBeanInstance();

    class LocalDateTimeFactory implements Factory {

        @Override
        public LocalDateTime create() {
            return LocalDateTime.now();
        }
    }

    class UuidFactory implements Factory {

        @Override
        public UUID create() {
            return UUID.randomUUID();
        }
    }
}
