package uk.gov.hmcts.reform.professionalapi.infrastructure.serialization;

public interface Serializer<T> {

    String serialize(T data);
}
