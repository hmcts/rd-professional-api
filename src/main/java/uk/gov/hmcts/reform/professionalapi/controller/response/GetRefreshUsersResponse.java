package uk.gov.hmcts.reform.professionalapi.controller.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.professionalapi.domain.RefreshUser;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
public class GetRefreshUsersResponse {

    private List<RefreshUser> users;
    private UUID lastRecordInPage;
    private boolean moreAvailable;

    public GetRefreshUsersResponse(List<RefreshUser> users, UUID lastRecordInPage, boolean moreAvailable) {
        this.users = users;
        this.lastRecordInPage = lastRecordInPage;
        this.moreAvailable = moreAvailable;
    }
}
