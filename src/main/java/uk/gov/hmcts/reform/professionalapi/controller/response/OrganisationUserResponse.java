package uk.gov.hmcts.reform.professionalapi.controller.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccessType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.professionalapi.util.RefDataUtil.fromUserConfiguredAccess;

@Setter
@Getter
@NoArgsConstructor
public class OrganisationUserResponse {
    public String userIdentifier;
    public String firstName;
    public String lastName;
    public String email;
    private LocalDateTime lastUpdated;
    private List<UserAccessType> userAccessTypes = new ArrayList<>();

    public OrganisationUserResponse(ProfessionalUser user) {
        this.userIdentifier = user.getUserIdentifier();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmailAddress();
        this.lastUpdated = user.getLastUpdated();
       this.userAccessTypes =
               user.getUserConfiguredAccesses().stream().map(uca -> fromUserConfiguredAccess(uca)).collect(toList());
    }
}
