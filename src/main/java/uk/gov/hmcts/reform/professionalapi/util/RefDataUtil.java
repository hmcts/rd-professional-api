package uk.gov.hmcts.reform.professionalapi.util;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.StringUtils.isBlank;
import static uk.gov.hmcts.reform.professionalapi.controller.advice.CcdErrorMessageResolver.resolveStatusAndReturnMessage;

import feign.FeignException;
import feign.Response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorResponse;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ExternalApiException;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ResourceNotFoundException;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.request.RetrieveUserProfilesRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.GetUserProfileResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.NewUserResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersEntityResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersResponse;
import uk.gov.hmcts.reform.professionalapi.domain.ModifyUserRolesResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMap;

@Slf4j
public class RefDataUtil {

    private RefDataUtil() {}

    @Value("${defaultPageSize}")
    public static final int DEFAULTPAGESIZE = 10;

    private  static final String UP_SERVICE_MSG = "Error while invoking UP";

    public static List<PaymentAccount> getPaymentAccountsFromUserAccountMap(List<UserAccountMap> userAccountMaps) {

        List<PaymentAccount> userMapPaymentAccount;

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

    public static List<SuperUser> getUserIdFromUserProfile(List<SuperUser> users, UserProfileFeignClient userProfileFeignClient, Boolean isRequiredRoles) {

        List<SuperUser> userProfileDtls = new ArrayList<>();
        ProfessionalUser professionalUser = null;
        for (SuperUser user: users) {
            professionalUser = getSingleUserIdFromUserProfile(user.toProfessionalUser(), userProfileFeignClient, isRequiredRoles);
            userProfileDtls.add(professionalUser.toSuperUser());
        }
        return userProfileDtls;
    }


    public static ProfessionalUser getSingleUserIdFromUserProfile(ProfessionalUser user, UserProfileFeignClient userProfileFeignClient, Boolean isRequiredRoles) {
        try (Response response =  userProfileFeignClient.getUserProfileById(user.getUserIdentifier())) {

            Class clazz = response.status() > 300 ? ErrorResponse.class : GetUserProfileResponse.class;
            ResponseEntity responseResponseEntity = JsonFeignResponseUtil.toResponseEntity(response, clazz);

            if (response.status() > 300) {
                ErrorResponse userProfileErrorResponse = (ErrorResponse) responseResponseEntity.getBody();
                throw new ExternalApiException(responseResponseEntity.getStatusCode(), userProfileErrorResponse.getErrorMessage());

            }
            mapUserInfo(user, responseResponseEntity, isRequiredRoles);
        }  catch (FeignException ex) {
            throw new ExternalApiException(HttpStatus.valueOf(ex.status()), UP_SERVICE_MSG);
        }

        return user;
    }

    public static List<Organisation> getMultipleUserProfilesFromUp(UserProfileFeignClient userProfileFeignClient,
                                                                   RetrieveUserProfilesRequest retrieveUserProfilesRequest,
                                                                   String showDeleted, Map<String, Organisation> activeOrganisationDetails) {
        Map<String, Organisation> modifiedOrgProfUserDetails = new HashMap<>();

        try (Response response = userProfileFeignClient.getUserProfiles(retrieveUserProfilesRequest, showDeleted,"false")) {


            Class clazz = response.status() > 300 ? ErrorResponse.class : ProfessionalUsersEntityResponse.class;
            ResponseEntity responseResponseEntity = JsonFeignResponseUtil.toResponseEntity(response, clazz);
            if (response.status() < 300) {

                modifiedOrgProfUserDetails = updateUserDetailsForActiveOrganisation(responseResponseEntity, activeOrganisationDetails);
            }

            return modifiedOrgProfUserDetails.values().stream().collect(Collectors.toList());
        }  catch (FeignException ex) {

            throw new ExternalApiException(HttpStatus.valueOf(ex.status()), UP_SERVICE_MSG);
        }

    }

    public static Map<String, Organisation> updateUserDetailsForActiveOrganisation(ResponseEntity responseEntity,
                                                                                   Map<String, Organisation> activeOrganisationDtls) {

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
            if (TRUE.equals(isRequiredRoles)) {
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

    public static ProfessionalUsersEntityResponse filterUsersByStatus(ResponseEntity responseEntity, String status) {

        if (responseEntity.getStatusCode().is2xxSuccessful() &&  null != responseEntity.getBody()) {

            ProfessionalUsersEntityResponse professionalUsersEntityResponse = (ProfessionalUsersEntityResponse) responseEntity.getBody();

            List<ProfessionalUsersResponse> filteredUsers =  professionalUsersEntityResponse.getUserProfiles().stream()
                    .filter(user -> status.equalsIgnoreCase(user.getIdamStatus()))
                    .collect(Collectors.toList());

            if (CollectionUtils.isEmpty(filteredUsers)) {
                throw new ResourceNotFoundException("No users found with status :" + status);
            }

            professionalUsersEntityResponse.setUserProfiles(filteredUsers);
            return professionalUsersEntityResponse;

        } else {
            throw new ExternalApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to retrieve Users from UP");
        }
    }

    public static HttpHeaders generateResponseEntityWithPaginationHeader(Pageable pageable, Page page, ResponseEntity responseEntity) {
        HttpHeaders headers = new HttpHeaders();

        final StringBuilder pageInformation = new StringBuilder();
        pageInformation.append("totalElements = " + page.getTotalElements());
        pageInformation.append(",");
        pageInformation.append("totalPages = " + page.getTotalPages());
        pageInformation.append(",");
        pageInformation.append("currentPage = " + pageable.getPageNumber());
        pageInformation.append(",");
        pageInformation.append("size = " + pageable.getPageSize());
        pageInformation.append(",");
        pageInformation.append("sortedBy = " + pageable.getSort());

        if (responseEntity == null) {
            headers.add("paginationInfo", pageInformation.toString());
        } else {
            // since Headers are read only , it can't be modified and hence copied all existing headers into new one and added new header for pagination
            MultiValueMap<String, String> originalHeaders = responseEntity.getHeaders();
            originalHeaders.forEach((key, value) -> headers.put(key, value));
            headers.put("paginationInfo", Collections.singletonList(pageInformation.toString()));
        }
        return headers;
    }

    public static Pageable createPageableObject(Integer page, Integer size, Sort sort) {
        if (size == null) {
            size = DEFAULTPAGESIZE;
        }
        return PageRequest.of(page, size, sort);
    }

    public static String getShowDeletedValue(String showDeleted) {
        if ("True".equalsIgnoreCase(showDeleted)) {
            showDeleted = "true";
        } else {
            showDeleted = "false";
        }
        return showDeleted;
    }

    public static ModifyUserRolesResponse decodeResponseFromUp(Response response) {
        ModifyUserRolesResponse modifyUserRolesResponse = new ModifyUserRolesResponse();
        boolean isFailureFromUp = response.status() > 300;
        Class clazz = isFailureFromUp ? ErrorResponse.class : ModifyUserRolesResponse.class;
        ResponseEntity responseEntity = JsonFeignResponseUtil.toResponseEntity(response, clazz);
        if (isFailureFromUp) {
            ErrorResponse errorResponse = (ErrorResponse) responseEntity.getBody();
            modifyUserRolesResponse.setErrorResponse(errorResponse);
        } else {
            modifyUserRolesResponse = (ModifyUserRolesResponse) responseEntity.getBody();
        }
        return modifyUserRolesResponse;
    }

    public static NewUserResponse findUserProfileStatusByEmail(String emailAddress, UserProfileFeignClient userProfileFeignClient) {

        NewUserResponse newUserResponse;
        try (Response response =  userProfileFeignClient.getUserProfileByEmail(emailAddress)) {

            Class clazz = response.status() > 300 ? ErrorResponse.class : NewUserResponse.class;
            ResponseEntity responseResponseEntity = JsonFeignResponseUtil.toResponseEntity(response, clazz);

            if (response.status() == 200) {

                newUserResponse = (NewUserResponse) responseResponseEntity.getBody();
            } else {
                ErrorResponse errorResponse = (ErrorResponse) responseResponseEntity.getBody();
                log.error("Response from UserProfileByEmail service call " + errorResponse.getErrorDescription());
                newUserResponse = new NewUserResponse();
            }

        }  catch (FeignException ex) {
            log.error("Error while invoking UserProfileByEmail service call", ex);
            throw new ExternalApiException(HttpStatus.valueOf(ex.status()), UP_SERVICE_MSG);
        }

        return newUserResponse;

    }

    public static void throwException(int statusCode) {
        log.info("Error status code: " + statusCode);
        HttpStatus httpStatus = HttpStatus.valueOf(statusCode);
        String errorMessage = resolveStatusAndReturnMessage(httpStatus);
        throw new ExternalApiException(httpStatus, errorMessage);
    }

    /* This method will convert string param to boolean.*/
    public static boolean getBooleanFromRolesRequiredParam(String rolesRequired) {
        return isBlank(rolesRequired) ? true : "false".equalsIgnoreCase(rolesRequired) ? FALSE : TRUE;
    }
}
