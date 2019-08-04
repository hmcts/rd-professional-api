package uk.gov.hmcts.reform.professionalapi.controller.request;

import static uk.gov.hmcts.reform.professionalapi.generator.ProfessionalApiGenerator.LENGTH_OF_ORGANISATION_IDENTIFIER;
import static uk.gov.hmcts.reform.professionalapi.generator.ProfessionalApiGenerator.ORGANISATION_IDENTIFIER_FORMAT_REGEX;

import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;

@Component
@Slf4j
public class OrganisationCreationRequestValidator {

    private final List<RequestValidator> validators;

    public OrganisationCreationRequestValidator(List<RequestValidator> validators) {
        this.validators = validators;
    }

    public void validate(OrganisationCreationRequest organisationCreationRequest) {
        validators.forEach(v -> v.validate(organisationCreationRequest));
    }

    public static boolean contains(String status) {
        for (OrganisationStatus type : OrganisationStatus.values()) {
            if (type.name().equalsIgnoreCase(status)) {
                return true;
            }
        }
        return false;
    }

    public void validateOrganisationIdentifier(String inputOrganisationIdentifier) {

        if (null == inputOrganisationIdentifier || LENGTH_OF_ORGANISATION_IDENTIFIER != inputOrganisationIdentifier.length() || !inputOrganisationIdentifier.matches(ORGANISATION_IDENTIFIER_FORMAT_REGEX)) {
            String errorMessage = "Invalid organisationIdentifier provided organisationIdentifier: " + inputOrganisationIdentifier;
            log.error(errorMessage);
            throw new EmptyResultDataAccessException(1);
        }
    }

    public void isOrganisationActive(Organisation organisation) {

        if (organisation == null) {
            log.error("Organisation not found");
            throw new EmptyResultDataAccessException("Organisation not found", 1);
        } else if (!organisation.isOrganisationStatusActive()) {
            log.error("Organisation is not active. Cannot add new users");
            throw new EmptyResultDataAccessException("Organisation is not active. Cannot add new users", 1);
        }
    }

    public static void validateJurisdictions(List<Map<String,String>> jurisdMaps, List<String> enumList) {

        if (CollectionUtils.isEmpty(jurisdMaps)) {
            throw new InvalidRequest("Jurisdictions not present");
        } else {
            jurisdMaps.forEach(jurisMap -> {
                if (jurisMap.size() > 1) {
                    throw new InvalidRequest("Jurisdictions should have only 1 value associated with id");
                } else if (jurisMap.isEmpty()) {
                    throw new InvalidRequest("Jurisdiction id should have at least 1 element");
                } else if (!"id".equals((String) jurisMap.keySet().toArray()[0])) {
                    throw new InvalidRequest("Jurisdictions key value should be 'id'");
                } else if (StringUtils.isBlank(jurisMap.get("id"))) {
                    throw new InvalidRequest("Jurisdictions value should not be blank or null");
                } else if (!enumList.contains(jurisMap.get("id"))) {
                    throw new InvalidRequest("Jurisdiction id not valid : " + jurisMap.get("id"));
                }
            });
        }
    }

    public static void validateJurisdictions(OrganisationCreationRequest organisationCreationRequest, List<String> enumList) {
        validateJurisdictions(organisationCreationRequest.getSuperUser().getJurisdictions(), enumList);
    }

    public static void validateJurisdictions(NewUserCreationRequest newUserCreationRequest, List<String> enumList) {
        validateJurisdictions(newUserCreationRequest.getJurisdictions(), enumList);
    }
}
