package uk.gov.hmcts.reform.professionalapi.controller.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.professionalapi.domain.RefreshUser;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class GetRefreshUsersResponse {

    private List<RefreshUser> users;
    private boolean moreAvailable;
}
