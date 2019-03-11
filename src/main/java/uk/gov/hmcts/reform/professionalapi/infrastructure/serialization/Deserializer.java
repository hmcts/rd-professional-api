package uk.gov.hmcts.reform.professionalapi.infrastructure.serialization;

public interface Deserializer<T> {

    T deserialize(String source);
}
