package uk.gov.hmcts.reform.professionalapi.util;

import static java.util.stream.Collectors.toList;

import feign.FeignException;
import feign.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorResponse;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ExternalApiException;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.request.RetrieveUserProfilesRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.GetUserProfileResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersEntityResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
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

        paymentAccountsFromOrg.addAll(paymentAccounts);
        return paymentAccounts;
    }

    public static List<ProfessionalUser> getUserIdFromUserProfile(List<ProfessionalUser> users, UserProfileFeignClient userProfileFeignClient, Boolean isRequiredRoles) {

        List<ProfessionalUser> userProfileDtls = new ArrayList<>();
        for (ProfessionalUser user: users) {

            userProfileDtls.add(getSingleUserIdFromUserProfile(user, userProfileFeignClient, isRequiredRoles));
        }
        return userProfileDtls;
    }


    public static ProfessionalUser getSingleUserIdFromUserProfile(ProfessionalUser user, UserProfileFeignClient userProfileFeignClient, Boolean isRequiredRoles) {
        try (Response response =  userProfileFeignClient.getUserProfileById(user.getUserIdentifier().toString())) {

            Class clazz = response.status() > 300 ? ErrorResponse.class : GetUserProfileResponse.class;
            ResponseEntity responseResponseEntity = JsonFeignResponseHelper.toResponseEntity(response, clazz);

            if (response.status() > 300) {
                ErrorResponse userProfileErrorResponse = (ErrorResponse) responseResponseEntity.getBody();
                throw new ExternalApiException(responseResponseEntity.getStatusCode(), userProfileErrorResponse.getErrorMessage());

            }
            mapUserInfo(user, responseResponseEntity, isRequiredRoles);
        }  catch (FeignException ex) {
            throw new ExternalApiException(HttpStatus.valueOf(ex.status()), "Error while invoking UP");
        }

        return user;
    }

    public static List<Organisation> getMultipleUserProfilesFromUp(UserProfileFeignClient userProfileFeignClient,
                                                         RetrieveUserProfilesRequest retrieveUserProfilesRequest,
                                                         String showDeleted, Map<UUID, Organisation> activeOrganisationDtls) {
        List<Organisation> modifiedOrgProfUserDtls = new ArrayList<>();
        Map<UUID, Organisation> modifiedOrgProfUserDetails = new HashMap<>();

        try (Response response = userProfileFeignClient.getUserProfiles(retrieveUserProfilesRequest, showDeleted,"false")) {


            Class clazz = response.status() > 300 ? ErrorResponse.class : ProfessionalUsersEntityResponse.class;
            ResponseEntity responseResponseEntity = JsonFeignResponseHelper.toResponseEntity(response, clazz);
            if (response.status() < 300) {

                modifiedOrgProfUserDetails = updateUserDetailsForActiveOrganisation(responseResponseEntity, activeOrganisationDtls);
            }

            return modifiedOrgProfUserDetails.values().stream().collect(Collectors.toList());
        }  catch (FeignException ex) {

            throw new ExternalApiException(HttpStatus.valueOf(ex.status()), "Error while invoking UP");
        }

    }

    public  static Map<UUID, Organisation> updateUserDetailsForActiveOrganisation(ResponseEntity responseEntity,
                                                                                  Map<UUID, Organisation> activeOrganisationDtls) {

        ProfessionalUsersEntityResponse professionalUsersEntityResponse = (ProfessionalUsersEntityResponse)responseEntity.getBody();
        if (null != professionalUsersEntityResponse
                && !CollectionUtils.isEmpty(professionalUsersEntityResponse.getUserProfiles())) {

            List<ProfessionalUsersResponse> userProfiles = professionalUsersEntityResponse.getUserProfiles();
            userProfiles.stream().forEach(userProfile -> {

                if (null != activeOrganisationDtls.get(userProfile.getUserIdentifier())) {

                    Organisation organisation = activeOrganisationDtls.get(userProfile.getUserIdentifier());

                    organisation.getUsers().get(0).setFirstName(userProfile.getFirstName());
                    organisation.getUsers().get(0).setLastName(userProfile.getLastName());
                    organisation.getUsers().get(0).setEmailAddress(userProfile.getEmail());

                    activeOrganisationDtls.put(userProfile.getUserIdentifier(), organisation);

                }

            });

        }
        return activeOrganisationDtls;
    }



    public static ProfessionalUser mapUserInfo(ProfessionalUser user, ResponseEntity responseResponseEntity, Boolean isRequiredRoles) {

        GetUserProfileResponse userProfileResponse = (GetUserProfileResponse) responseResponseEntity.getBody();
        if (!StringUtils.isEmpty(userProfileResponse)) {
            user.setFirstName(userProfileResponse.getFirstName());
            user.setLastName(userProfileResponse.getLastName());
            user.setEmailAddress(userProfileResponse.getEmail());
            if (isRequiredRoles) {
                user.setUserIdentifier(userProfileResponse.getIdamId());
                user.setIdamStatus(userProfileResponse.getIdamStatus());
                user.setRoles(userProfileResponse.getRoles());
                user.setIdamStatusCode(userProfileResponse.getIdamStatusCode());
                user.setIdamMessage(userProfileResponse.getIdamMessage());
            }
        }
        return user;
    }

    public static String removeEmptySpaces(String value) {
        String modValue = value;
        if (!StringUtils.isEmpty(modValue)) {
            modValue = value.trim().replaceAll("\\s+", " ");
        }
        return modValue;
    }

    public static String removeAllSpaces(String value) {
        String modValue = value;
        if (!StringUtils.isEmpty(modValue)) {
            modValue = modValue.replaceAll("\\s+", "");
        }
        return modValue;
    }

    public static void validateOrgIdentifier(String extOrgId, String orgId) {

        if (!extOrgId.trim().equals(orgId.trim())) {

            throw new AccessDeniedException("403 Forbidden");
        }

    }
}
