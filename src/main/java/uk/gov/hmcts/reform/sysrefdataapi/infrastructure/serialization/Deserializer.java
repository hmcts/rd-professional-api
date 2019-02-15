package uk.gov.hmcts.reform.sysrefdataapi.infrastructure.serialization;

public interface Deserializer<T> {

    T deserialize(String source);
}
