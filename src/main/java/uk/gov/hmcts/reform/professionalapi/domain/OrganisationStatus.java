package uk.gov.hmcts.reform.professionalapi.domain;

public enum OrganisationStatus {
   PENDING,
   ACTIVE,
   BLOCKED,
   DELETED;

   public boolean isActive() {
      return this == ACTIVE;
   }

   public boolean isPending() {
      return this == PENDING;
   }

}
