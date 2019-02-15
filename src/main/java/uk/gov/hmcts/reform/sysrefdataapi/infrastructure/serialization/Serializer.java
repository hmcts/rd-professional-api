package uk.gov.hmcts.reform.sysrefdataapi.infrastructure.serialization;

public interface Serializer<T> {

    String serialize(T data);
}
