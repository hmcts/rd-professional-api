package uk.gov.hmcts.reform.professionalapi.domain;

public enum OrganisationStatus {
    PENDING("pending"),
    ACTIVE("active"),
    BLOCKED("blocked"),
    DELETED("deleted");

    private String status;

    OrganisationStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return  status;
    }
}
