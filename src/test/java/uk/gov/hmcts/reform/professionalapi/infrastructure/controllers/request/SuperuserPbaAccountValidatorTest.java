package uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request.PbaAccountCreationRequest.aPbaPaymentAccount;
import static uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request.UserCreationRequest.aUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures.someMinimalOrganisationRequest;

import org.junit.Test;

public class SuperuserPbaAccountValidatorTest {

    private final SuperuserPbaAccountValidator validator = new SuperuserPbaAccountValidator();

    @Test
    public void throws_when_super_user_account_provided_but_no_organisation_account() {

        PbaAccountCreationRequest superUserAccount = aPbaPaymentAccount()
                .build();

        UserCreationRequest superUser = aUserCreationRequest()
                .pbaAccount(superUserAccount)
                .build();

        OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest()
                .pbaAccounts(null)
                .superUser(superUser)
                .build();

        assertThatThrownBy(() -> validator.validate(organisationCreationRequest))
            .isExactlyInstanceOf(InvalidRequest.class)
            .hasMessage("Super user pba account number not in the organisations accounts");
    }

    @Test
    public void throws_when_super_user_account_provided_but_no_corresponding_organisation_account() {

        PbaAccountCreationRequest superUserAccount = aPbaPaymentAccount()
                .pbaNumber("1")
                .build();

        PbaAccountCreationRequest organisationAccount1 = aPbaPaymentAccount()
                .pbaNumber("abc")
                .build();

        PbaAccountCreationRequest organisationAccount2 = aPbaPaymentAccount()
                .pbaNumber("xzy")
                .build();

        UserCreationRequest superUser = aUserCreationRequest()
                .pbaAccount(superUserAccount)
                .build();

        OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest()
                .pbaAccounts(asList(
                        organisationAccount1,
                        organisationAccount2))
                .superUser(superUser)
                .build();

        assertThatThrownBy(() -> validator.validate(organisationCreationRequest))
                .isExactlyInstanceOf(InvalidRequest.class)
                .hasMessage("Super user pba account number not in the organisations accounts");
    }
}