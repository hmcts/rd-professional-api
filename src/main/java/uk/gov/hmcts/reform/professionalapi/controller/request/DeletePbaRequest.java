package uk.gov.hmcts.reform.professionalapi.controller.request;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class DeletePbaRequest {

    private Set<String> paymentAccounts;

}
