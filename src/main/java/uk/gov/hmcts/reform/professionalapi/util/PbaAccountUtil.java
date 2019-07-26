package uk.gov.hmcts.reform.professionalapi.util;

import static java.util.stream.Collectors.toList;

import feign.Response;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorResponse;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.response.GetUserProfileResponse;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMap;

public interface PbaAccountUtil {

    public static List<PaymentAccount> getPaymentAccountsFromUserAccountMap(List<UserAccountMap> userAccountMaps) {

        List<PaymentAccount> userMapPaymentAccount = new ArrayList<>();

        userMapPaymentAccount = userAccountMaps.stream().map(userAccountMap -> userAccountMap.getUserAccountMapId().getPaymentAccount()).collect(toList());

        return userMapPaymentAccount;
    }


    public static List<PaymentAccount> getPaymentAccountFromUserMap(List<PaymentAccount> userMapPaymentAccount, List<PaymentAccount> paymentAccountsEntity) {

        List<PaymentAccount> paymentAccounts = new ArrayList<>();

        if (!paymentAccountsEntity.isEmpty()) {

            paymentAccountsEntity.forEach(paymentAccount -> {
                for (PaymentAccount usrMapPaymentAccount : userMapPaymentAccount) {
                    if (usrMapPaymentAccount.getId().equals(paymentAccount.getId())) {

                        paymentAccounts.add(paymentAccount);

                    }
                }
            });
        }
        return paymentAccounts;
    }

    public static List<PaymentAccount> getPaymentAccount(List<PaymentAccount> paymentAccounts) {

        List<PaymentAccount> paymentAccountsFromOrg = new ArrayList<>();

        if (!paymentAccounts.isEmpty()) {

            paymentAccounts.forEach(paymentAccount -> {

                paymentAccountsFromOrg.add(paymentAccount);
            });
        }
        return paymentAccounts;
    }

    public static List<ProfessionalUser> getUserIdFromUserProfile(List<ProfessionalUser> users, UserProfileFeignClient userProfileFeignClient) {

        List<ProfessionalUser> userProfileDtls = new ArrayList<>();
        for (ProfessionalUser user : users) {

            Response response = userProfileFeignClient.getUserProfileByEmail(user.getUserIdentifier().toString());

            Class clazz = response.status() > 300 ? ErrorResponse.class : GetUserProfileResponse.class;
            ResponseEntity responseResponseEntity = JsonFeignResponseHelper.toResponseEntity(response, clazz);

            mapUserInfo(user, responseResponseEntity);
            userProfileDtls.add(user);
        }

        return userProfileDtls;
    }

    public static ProfessionalUser mapUserInfo(ProfessionalUser user, ResponseEntity responseResponseEntity) {

        GetUserProfileResponse userProfileResponse = (GetUserProfileResponse) responseResponseEntity.getBody();
        if (!StringUtils.isEmpty(userProfileResponse)) {

            user.setFirstName(userProfileResponse.getFirstName());
            user.setLastName(userProfileResponse.getLastName());
            user.setEmailAddress(userProfileResponse.getEmail());
        }
        return user;
    }

    public static String removeEmptySpaces(String value) {
        String modValue = "";
        if (value != null && !value.trim().isEmpty()) {
            modValue = value.trim();
            modValue = modValue.replaceAll("\\s+", " ");
            return modValue;
        } else {
            return value;
        }
    }

    public static String removeAllSpaces(String value) {
        String modValue = "";
        if (value != null && !value.trim().isEmpty()) {
            modValue = value.trim();
            modValue = modValue.replaceAll("\\s+", "");
            return modValue;
        } else {
            return value;
        }
    }
}
