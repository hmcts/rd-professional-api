package uk.gov.hmcts.reform.professionalapi.domain;

public enum OrganisationStatus {
   PENDING,
   ACTIVE,
   BLOCKED,
   DELETED,
   REVIEW;

    public boolean isPending() {
        return this == PENDING;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }
}
