package uk.gov.hmcts.reform.professionalapi.persistence.Impl;


import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.persistence.OrganisationRepository;

import java.util.List;


public abstract class OrganisationRepositoryImpl implements OrganisationRepository {


    public List<Organisation> findOrgsByStatus () {

        return null;
    }

    public Organisation findOrgsById () {

        return null;
    }
}
