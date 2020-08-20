package uk.gov.hmcts.reform.professionalapi.util;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.professionalapi.controller.advice.CcdErrorMessageResolver.resolveStatusAndReturnMessage;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.DELETION_SUCCESS_MSG;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_CODE_500;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_MESSAGE_UP_FAILED;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.PRD_AAC_SYSTEM;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.STATUS_CODE_204;

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
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorResponse;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ExternalApiException;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ResourceNotFoundException;
import uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.request.DeleteUserProfilesRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.RetrieveUserProfilesRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.DeleteOrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.GetUserProfileResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.NewUserResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersEntityResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersEntityResponseWithoutRoles;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersResponseWithoutRoles;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMap;

@Component
@Slf4j
public class RefDataUtil {

    private RefDataUtil() {
    }

    private static int defaultPageSize;

    private static String loggingComponentName;

    public static List<PaymentAccount> getPaymentAccountsFromUserAccountMap(List<UserAccountMap> userAccountMaps) {

        List<PaymentAccount> userMapPaymentAccount;

        userMapPaymentAccount = userAccountMaps.stream().map(userAccountMap -> userAccountMap.getUserAccountMapId()
                .getPaymentAccount()).collect(toList());

        return userMapPaymentAccount;
    }


    public static List<PaymentAccount> getPaymentAccountFromUserMap(List<PaymentAccount> userMapPaymentAccount,
                                                                    List<PaymentAccount> paymentAccountsEntity) {

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

    public static List<SuperUser> getUserIdFromUserProfile(List<SuperUser> users,
                                                           UserProfileFeignClient userProfileFeignClient,
                                                           Boolean isRequiredRoles) {

        List<SuperUser> userProfileDtls = new ArrayList<>();
        ProfessionalUser professionalUser = null;
        for (SuperUser user : users) {
            professionalUser = getSingleUserIdFromUserProfile(user.toProfessionalUser(), userProfileFeignClient,
                    isRequiredRoles);
            userProfileDtls.add(professionalUser.toSuperUser());
        }
        return userProfileDtls;
    }


    public static ProfessionalUser getSingleUserIdFromUserProfile(ProfessionalUser user,
                                                                  UserProfileFeignClient userProfileFeignClient,
                                                                  Boolean isRequiredRoles) {
        try (Response response = userProfileFeignClient.getUserProfileById(user.getUserIdentifier())) {

            Object clazz = response.status() > 300 ? ErrorResponse.class : GetUserProfileResponse.class;
            ResponseEntity<Object> responseResponseEntity = JsonFeignResponseUtil.toResponseEntity(response, clazz);

            if (response.status() > 300) {
                ErrorResponse userProfileErrorResponse = (ErrorResponse) responseResponseEntity.getBody();
                throw new ExternalApiException(responseResponseEntity.getStatusCode(),
                        userProfileErrorResponse.getErrorMessage());

            }
            mapUserInfo(user, responseResponseEntity, isRequiredRoles);
        } catch (FeignException ex) {
            throw new ExternalApiException(HttpStatus.valueOf(ex.status()), ERROR_MESSAGE_UP_FAILED);
        }

        return user;
    }

    public static List<Organisation>
        getMultipleUserProfilesFromUp(UserProfileFeignClient userProfileFeignClient,
                                  RetrieveUserProfilesRequest retrieveUserProfilesRequest,
                                  String showDeleted, Map<String,
            Organisation> activeOrganisationDetails) {
        Map<String, Organisation> modifiedOrgProfUserDetails = new HashMap<>();

        try (Response response = userProfileFeignClient.getUserProfiles(retrieveUserProfilesRequest, showDeleted,
                "false")) {


            Object clazz = response.status() > 300 ? ErrorResponse.class : ProfessionalUsersEntityResponse.class;
            ResponseEntity<Object> responseResponseEntity = JsonFeignResponseUtil.toResponseEntity(response, clazz);
            if (response.status() < 300) {

                modifiedOrgProfUserDetails = updateUserDetailsForActiveOrganisation(responseResponseEntity,
                        activeOrganisationDetails);
            }

            return new ArrayList<>(modifiedOrgProfUserDetails.values());
        } catch (FeignException ex) {

            throw new ExternalApiException(HttpStatus.valueOf(ex.status()), ERROR_MESSAGE_UP_FAILED);
        }

    }

    public static Map<String, Organisation>
        updateUserDetailsForActiveOrganisation(ResponseEntity<Object> responseEntity,
                                           Map<String, Organisation> activeOrganisationDtls) {

        ProfessionalUsersEntityResponse professionalUsersEntityResponse
                = (ProfessionalUsersEntityResponse) responseEntity.getBody();
        if (null != professionalUsersEntityResponse
                && !CollectionUtils.isEmpty(professionalUsersEntityResponse.getUserProfiles())) {

            List<ProfessionalUsersResponse> userProfiles = professionalUsersEntityResponse.getUserProfiles();
            userProfiles.forEach(userProfile -> {

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

    public static ProfessionalUser mapUserInfo(ProfessionalUser user, ResponseEntity<Object> responseResponseEntity,
                                               Boolean isRequiredRoles) {

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

    public static Object filterUsersByStatus(ResponseEntity<Object> responseEntity, String status) {

        if (responseEntity.getStatusCode().is2xxSuccessful() && null != responseEntity.getBody()) {

            if (responseEntity.getBody() instanceof ProfessionalUsersEntityResponse) {

                return filterUsersByStatusWithRoles((ProfessionalUsersEntityResponse) responseEntity.getBody(),
                        status);

            } else {

                return filterUsersByStatusWithoutRoles((ProfessionalUsersEntityResponseWithoutRoles) responseEntity
                        .getBody(), status);
            }

        } else {
            throw new ExternalApiException(HttpStatus.INTERNAL_SERVER_ERROR, ERROR_MESSAGE_UP_FAILED);
        }
    }

    public static ProfessionalUsersEntityResponse
        filterUsersByStatusWithRoles(ProfessionalUsersEntityResponse professionalUsersEntityResponse, String status) {
        List<ProfessionalUsersResponse> filteredUsers = professionalUsersEntityResponse
                .getUserProfiles().stream()
                    .filter(user -> status.equalsIgnoreCase(user.getIdamStatus()))
                        .collect(Collectors.toList());

        checkListIsEmpty(filteredUsers, status);

        professionalUsersEntityResponse.setUserProfiles(filteredUsers);
        return professionalUsersEntityResponse;
    }

    public static ProfessionalUsersEntityResponseWithoutRoles
        filterUsersByStatusWithoutRoles(
                ProfessionalUsersEntityResponseWithoutRoles professionalUsersEntityResponseWithoutRoles,
                String status) {
        List<ProfessionalUsersResponseWithoutRoles> filteredUsers
                = professionalUsersEntityResponseWithoutRoles.getUserProfiles().stream()
                .filter(user -> status.equalsIgnoreCase(user.getIdamStatus()))
                .collect(Collectors.toList());

        checkListIsEmpty(filteredUsers, status);

        professionalUsersEntityResponseWithoutRoles.setUserProfiles(filteredUsers);
        return professionalUsersEntityResponseWithoutRoles;
    }

    public static void checkListIsEmpty(List<? extends ProfessionalUsersResponseWithoutRoles> filteredUsers,
                                        String status) {
        if (CollectionUtils.isEmpty(filteredUsers)) {
            throw new ResourceNotFoundException("No users found with status :" + status);
        }
    }

    public static HttpHeaders generateResponseEntityWithPaginationHeader(Pageable pageable, Page<?> page,
                                                                         ResponseEntity<Object> responseEntity) {
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
            // since Headers are read only , it can't be modified.
            // Hence copied all existing headers into new one and added new header for pagination
            MultiValueMap<String, String> originalHeaders = responseEntity.getHeaders();
            originalHeaders.forEach(headers::put);
            headers.put("paginationInfo", Collections.singletonList(pageInformation.toString()));
        }
        return headers;
    }

    public static Pageable createPageableObject(Integer page, Integer size, Sort sort) {
        if (size == null) {
            size = defaultPageSize;
        }
        return PageRequest.of(page, size, sort);
    }

    public static String getShowDeletedValue(String showDeleted) {
        return ProfessionalApiConstants.TRUE.equalsIgnoreCase(showDeleted)
                ? ProfessionalApiConstants.TRUE : ProfessionalApiConstants.FALSE;
    }

    public static Boolean getReturnRolesValue(Boolean returnRoles) {
        return FALSE.equals(returnRoles) ? FALSE : TRUE;
    }

    public static NewUserResponse findUserProfileStatusByEmail(String emailAddress,
                                                               UserProfileFeignClient userProfileFeignClient) {

        NewUserResponse newUserResponse;
        try (Response response = userProfileFeignClient.getUserProfileByEmail(emailAddress)) {

            Object clazz = response.status() > 300 ? ErrorResponse.class : NewUserResponse.class;
            ResponseEntity<Object> responseResponseEntity = JsonFeignResponseUtil.toResponseEntity(response, clazz);

            if (response.status() == 200) {

                newUserResponse = (NewUserResponse) responseResponseEntity.getBody();
            } else {
                ErrorResponse errorResponse = (ErrorResponse) responseResponseEntity.getBody();
                log.error("{}:: Response from UserProfileByEmail service call {}",
                        loggingComponentName, errorResponse.getErrorDescription());
                newUserResponse = new NewUserResponse();
            }

        } catch (FeignException ex) {
            log.error("{}::", loggingComponentName + "Error while invoking UserProfileByEmail service call", ex);
            throw new ExternalApiException(HttpStatus.valueOf(ex.status()), ERROR_MESSAGE_UP_FAILED);
        }

        return newUserResponse;

    }

    public static DeleteOrganisationResponse deleteUserProfilesFromUp(DeleteUserProfilesRequest deleteUserRequest,
            UserProfileFeignClient userProfileFeignClient) {

        DeleteOrganisationResponse deleteOrganisationResponse = null;
        try (Response response = userProfileFeignClient.deleteUserProfile(deleteUserRequest)) {

            if (STATUS_CODE_204 == response.status()) {
                deleteOrganisationResponse = new DeleteOrganisationResponse(STATUS_CODE_204, DELETION_SUCCESS_MSG);
            } else if (ERROR_CODE_500 <= response.status()) {
                log.error("DeleteUserProfiles service call failed in PRD::" + response.reason());
                deleteOrganisationResponse = new DeleteOrganisationResponse(ERROR_CODE_500, ERROR_MESSAGE_UP_FAILED);
            }

        } catch (FeignException ex) {
            log.error("DeleteUserProfiles service call failed in PRD:: " + ex);
            throw new ExternalApiException(HttpStatus.valueOf(ex.status()), ERROR_MESSAGE_UP_FAILED);

        }
        return deleteOrganisationResponse;
    }

    public static void throwException(int statusCode) {
        log.info("{}:: Error status code: " + statusCode, loggingComponentName);
        HttpStatus httpStatus = HttpStatus.valueOf(statusCode);
        String errorMessage = resolveStatusAndReturnMessage(httpStatus);
        throw new ExternalApiException(httpStatus, errorMessage);
    }

    public static ResponseEntity<Object> setOrgIdInGetUserResponse(ResponseEntity<Object> responseEntity,
                                                                   String organisationIdentifier) {
        ResponseEntity<Object> newResponseEntity;
        if (responseEntity.getBody() instanceof ProfessionalUsersEntityResponse) {
            ProfessionalUsersEntityResponse professionalUsersEntityResponse
                    = (ProfessionalUsersEntityResponse) responseEntity.getBody();
            professionalUsersEntityResponse.setOrganisationIdentifier(organisationIdentifier);
            newResponseEntity = new ResponseEntity<>(professionalUsersEntityResponse, responseEntity.getHeaders(),
                    responseEntity.getStatusCode());
        } else {
            ProfessionalUsersEntityResponseWithoutRoles professionalUsersEntityResponseWithoutRoles
                    = (ProfessionalUsersEntityResponseWithoutRoles) responseEntity.getBody();
            professionalUsersEntityResponseWithoutRoles.setOrganisationIdentifier(organisationIdentifier);
            newResponseEntity = new ResponseEntity<>(professionalUsersEntityResponseWithoutRoles,
                    responseEntity.getHeaders(), responseEntity.getStatusCode());
        }
        return newResponseEntity;
    }

    @Value("${loggingComponentName}")
    public void setLoggingComponentName(String loggingComponentName) {
        RefDataUtil.loggingComponentName = loggingComponentName;
    }

    @Value("${defaultPageSize}")
    public void setDefaultPageSize(int defaultPageSize) {
        RefDataUtil.defaultPageSize = defaultPageSize;
    }

    public static boolean isSystemRoleUser(List<String> roles) {
        return roles.size() == 1 && roles.contains(PRD_AAC_SYSTEM);
    }
}
