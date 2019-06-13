package uk.gov.hmcts.reform.professionalapi.utils;

import java.lang.reflect.Field;


public interface ReflectionHelper {

    @SuppressWarnings("unchecked")
    static <T,U> U getPrivateField(T instance, String fieldName) {
        U result;
        try {
            Field f = instance.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            result = (U) f.get(instance);
        } catch (Exception ex) {
            System.err.println("Could not make reflective call");
            return null;
        }
        return result;
    }
}
