package uk.gov.hmcts.reform.professionalapi.serialization;

public interface Deserializer<T> {

    T deserialize(String source);
}
