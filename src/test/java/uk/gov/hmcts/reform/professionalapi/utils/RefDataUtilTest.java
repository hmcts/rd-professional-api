package uk.gov.hmcts.reform.professionalapi.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.reform.professionalapi.controller.response.GetUserProfileResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMap;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMapId;
import uk.gov.hmcts.reform.professionalapi.util.RefDataUtil;

public class RefDataUtilTest {

    @Test
    public void shouldReturnPaymentAccountsFromUserAccountMap() {

        PaymentAccount paymentAccount = mock(PaymentAccount.class);
        UserAccountMapId userAccountMapId = mock(UserAccountMapId.class);

        UserAccountMap userAccountMap = mock(UserAccountMap.class);

        List<UserAccountMap> userAccountMaps = new ArrayList<>();
        userAccountMaps.add(userAccountMap);

        when(userAccountMap.getUserAccountMapId()).thenReturn(userAccountMapId);
        when(userAccountMapId.getPaymentAccount()).thenReturn(paymentAccount);
        List<PaymentAccount> paymentAccounts = RefDataUtil.getPaymentAccountsFromUserAccountMap(userAccountMaps);
        assertThat(paymentAccounts.size()).isGreaterThan(0);
    }

    @Test
    public void shouldReturnPaymentAccountFromUserMap() {

        final UUID paymentAccountUuid = UUID.randomUUID();

        PaymentAccount paymentAccountMock = mock(PaymentAccount.class);

        when(paymentAccountMock.getId()).thenReturn(paymentAccountUuid);

        List<PaymentAccount> userMapPaymentAccount = new ArrayList<>();

        userMapPaymentAccount.add(paymentAccountMock);

        List<PaymentAccount> paymentAccountsEntity = new ArrayList<>();

        paymentAccountsEntity.add(paymentAccountMock);

        List<PaymentAccount> paymentAccounts = RefDataUtil.getPaymentAccountFromUserMap(userMapPaymentAccount, paymentAccountsEntity);

        assertThat(paymentAccounts.size()).isGreaterThan(0);
    }

    @Test
    public void shouldReturnPaymentAccountFromOrganisationUser() {

        PaymentAccount paymentAccountMock = mock(PaymentAccount.class);

        List<PaymentAccount> paymentAccountsEntity = new ArrayList<>();

        paymentAccountsEntity.add(paymentAccountMock);

        if (!paymentAccountsEntity.isEmpty()) {

            List<PaymentAccount> paymentAccounts = RefDataUtil.getPaymentAccount(paymentAccountsEntity);
            assertThat(paymentAccounts.size()).isGreaterThan(0);
        }
    }

    @Test
    public void removeEmptyWhiteSpacesTest() {

        assertThat(RefDataUtil.removeEmptySpaces(" Test ")).isEqualTo("Test");
        assertThat(RefDataUtil.removeEmptySpaces(null)).isEqualTo(null);
        assertThat(RefDataUtil.removeEmptySpaces(" Te  st ")).isEqualTo("Te st");

    }

    @Test
    public void removeAllWhiteSpacesTest() {

        assertThat(RefDataUtil.removeAllSpaces(" T e s t    1 ")).isEqualTo("Test1");
        assertThat(RefDataUtil.removeAllSpaces(null)).isEqualTo(null);

    }

    @Test(expected = AccessDeniedException.class)
    public void shouldReturnTrueValidateOrgIdentifier() {
        String uuid = UUID.randomUUID().toString();
        RefDataUtil.validateOrgIdentifier(uuid,UUID.randomUUID().toString());
    }

    @Test
    public void mapUserInfoCorrectly() {

        ProfessionalUser professionalUser = new ProfessionalUser();
        professionalUser.setFirstName("abc");
        professionalUser.setLastName("bcd");
        professionalUser.setEmailAddress("a@b.co.uk");
        professionalUser.setIdamStatusCode("200");
        professionalUser.setIdamMessage("Success");
        ResponseEntity responseEntity = mock(ResponseEntity.class);

        ProfessionalUser mappedUser = RefDataUtil.mapUserInfo(professionalUser, responseEntity, true);

        assertThat(mappedUser).isNotNull();

    }

    @Test
    public void test_mapUserInfo() {
        UUID id = UUID.randomUUID();
        ProfessionalUser userMock = new ProfessionalUser();
        ResponseEntity responseResponseEntityMock = mock(ResponseEntity.class);
        GetUserProfileResponse getUserProfileResponseMock = mock(GetUserProfileResponse.class);
        when(responseResponseEntityMock.getBody()).thenReturn(getUserProfileResponseMock);
        when(getUserProfileResponseMock.getFirstName()).thenReturn("fname");
        when(getUserProfileResponseMock.getLastName()).thenReturn("lname");
        when(getUserProfileResponseMock.getEmail()).thenReturn("some@hmcts.net");
        when(getUserProfileResponseMock.getIdamStatus()).thenReturn(IdamStatus.ACTIVE);
        when(getUserProfileResponseMock.getIdamId()).thenReturn(id);
        when(getUserProfileResponseMock.getRoles()).thenReturn(new ArrayList<String>());
        when(getUserProfileResponseMock.getIdamStatusCode()).thenReturn("code");
        when(getUserProfileResponseMock.getIdamMessage()).thenReturn("test error message");
        ProfessionalUser responseUser = RefDataUtil.mapUserInfo(userMock, responseResponseEntityMock, true);
        assertThat(responseUser).isNotNull();
        assertThat(responseUser.getEmailAddress()).isEqualTo("some@hmcts.net");
        assertThat(responseUser.getFirstName()).isEqualTo("fname");
        assertThat(responseUser.getLastName()).isEqualTo("lname");
        assertThat(responseUser.getIdamStatus()).isEqualTo(IdamStatus.ACTIVE);
        assertThat(responseUser.getUserIdentifier()).isEqualTo(id);
        assertThat(responseUser.getRoles()).isNotNull();
        assertThat(responseUser.getUserIdentifier()).isEqualTo(id);
        assertThat(getUserProfileResponseMock.getIdamStatusCode()).isEqualTo("code");
        assertThat(getUserProfileResponseMock.getIdamMessage()).isEqualTo("test error message");
    }
}
