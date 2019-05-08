package uk.gov.hmcts.reform.professionalapi.serialization;

public interface Serializer<T> {

    String serialize(T data);
}
