package uk.gov.hmcts.reform.professionalapi.sort;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

public interface ProfessionalApiSort {

    /**
     * Sorts Professional user list with Created date i.e most oldest professional user will appear first
     * @param organisation organisation for which it users need to be sorted
     * @return List of ProfessionalUser
     */
    static List<ProfessionalUser> sortUserListByCreatedDate(Organisation organisation) {
        return organisation.getUsers().stream().sorted((Comparator.comparing(ProfessionalUser::getCreated))).collect(Collectors.toList());
    }
}
