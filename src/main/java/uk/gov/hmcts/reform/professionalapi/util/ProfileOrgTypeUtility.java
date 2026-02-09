package uk.gov.hmcts.reform.professionalapi.util;

import org.springframework.stereotype.Component;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;

@Component
public class ProfileOrgTypeUtility {

    private ProfileOrgTypeUtility() {
    }

    /*GOVT("GOVT","Government Organisation",
                 "GOVERNMENT_ORGANISATION_PROFILE"),

    OTHER("OTHER","Other",
                  "ORGANISATION_PROFILE"),

    BARR("BARR","Barrister",
                 "BARR_PROFILE","ORGANISATION_PROFILE"),

    SOLICITOR("SOLICITOR","Solicitor",
                      "SOLICITOR_PROFILE","ORGANISATION_PROFILE"),

    LOCALAUTH("LOCALAUTH","Local Authority",
                      "LOCALAUTH_PROFILE","ORGANISATION_PROFILE"),

    PROBPRAC("PROBPRAC","Probate Practitioner",
                     "PROBPRAC_PROFILE","ORGANISATION_PROFILE"),

    OTHER_ACCOUNT("OTHER-ACCOUNT","Accountancy, banking and finance",
                          "OTHER_ACCOUNT_PROFILE","ORGANISATION_PROFILE"),

    OTHER_PUBDEF("OTHER-PUBDEF","Public sector & defence",
                         "OTHER_PUBDEF_PROFILE","ORGANISATION_PROFILE"),

    OTHER_ENV("OTHER-ENV","Environment and agriculture",
                      "OTHER_ENV_PROFILE","ORGANISATION_PROFILE"),

    OTHER_MARK("OTHER-MARK","Marketing, advertising and PR",
                       "OTHER_MARK_PROFILE","ORGANISATION_PROFILE"),

    OTHER_RETAIL("OTHER-RETAIL","Retail & wholesale",
                         "OTHER_RETAIL_PROFILE","ORGANISATION_PROFILE"),

    OTHER_TRANSP("OTHER-TRANSP","Transport and logistics",
                         "OTHER_TRANSP_PROFILE","ORGANISATION_PROFILE"),

    OTHER_ACCOM("OTHER-ACCOM","Accommodation & food",
                        "OTHER_ACCOM_PROFILE","ORGANISATION_PROFILE"),

    OTHER_SOCIAL("OTHER-SOCIAL","Social care",
                         "OTHER_SOCIAL_PROFILE","ORGANISATION_PROFILE"),

    OTHER_SCIENCE("OTHER-SCIENCE","Science and pharmaceuticals",
                          "OTHER_SCIENCE_PROFILE","ORGANISATION_PROFILE"),

    OTHER_ENERGY("OTHER-ENERGY","Energy and utilities",
                         "OTHER_ENERGY_PROFILE","ORGANISATION_PROFILE"),

    OTHER_ENGG("OTHER-ENGG","Engineering and manufacturing",
                       "OTHER_ENGG_PROFILE","ORGANISATION_PROFILE"),

    OTHER_CHARITY("OTHER-CHARITY","Charity and voluntary work",
                          "OTHER_CHARITY_PROFILE","ORGANISATION_PROFILE"),

    OTHER_MINING("OTHER-MINING","Mining and quarrying",
                         "OTHER_MINING_PROFILE","ORGANISATION_PROFILE"),

    OTHER_PUBADM("OTHER-PUBADM","Public services and administration",
                         "OTHER_PUBADM_PROFILE","ORGANISATION_PROFILE"),

    OTHER_LAW("OTHER-LAW","Law enforcement and security",
                      "OTHER_LAW_PROFILE","ORGANISATION_PROFILE"),

    OTHER_MEDIA("OTHER-MEDIA","Media and internet",
                        "OTHER_MEDIA_PROFILE","ORGANISATION_PROFILE"),

    OTHER_HOSP("OTHER-HOSP","Hospitality and events management",
                       "OTHER_HOSP_PROFILE","ORGANISATION_PROFILE"),

    OTHER_FIN("OTHER-FIN","Financial services",
                      "OTHER_FIN_PROFILE","ORGANISATION_PROFILE"),

    OTHER_LEISURE("OTHER-LEISURE","Leisure, sport and tourism",
                          "OTHER_LEISURE_PROFILE","ORGANISATION_PROFILE"),

    OTHER_REC("OTHER-REC","Recruitment and HR",
                      "OTHER_REC_PROFILE","ORGANISATION_PROFILE"),

    OTHER_CREATI("OTHER-CREATI","Creative arts and design",
                         "OTHER_CREATI_PROFILE","ORGANISATION_PROFILE"),

    OTHER_SALES("OTHER-SALES","Sales",
                        "OTHER_SALES_PROFILE","ORGANISATION_PROFILE"),

    OTHER_ITCOMM("OTHER-ITCOMM","IT & communications",
                         "OTHER_ITCOMM_PROFILE","ORGANISATION_PROFILE"),

    OTHER_HEALTH("OTHER-HEALTH","Healthcare",
                         "OTHER_HEALTH_PROFILE","ORGANISATION_PROFILE"),

    OTHER_BUSI("OTHER-BUSI","Business, consulting and management",
                       "OTHER_BUSI_PROFILE","ORGANISATION_PROFILE"),

    OTHER_ADMIN("OTHER-ADMIN","Admin and support",
                        "OTHER_ADMIN_PROFILE","ORGANISATION_PROFILE"),

    OTHER_NFP("OTHER-NFP","Not for profit",
                      "OTHER_NFP_PROFILE","ORGANISATION_PROFILE"),

    OTHER_PROP("OTHER-PROP","Property and construction",
                       "OTHER_PROP_PROFILE","ORGANISATION_PROFILE"),

    OTHER_EDU("OTHER-EDU","Education",
                      "OTHER_EDU_PROFILE","ORGANISATION_PROFILE"),

    OTHER_REALT("OTHER-REALT","Real estate activities",
                        "OTHER_REALT_PROFILE","ORGANISATION_PROFILE"),

    GOVT_DWP("GOVT-DWP","Government Organisation DWP",
                     "GOVT_DWP_PROFILE","GOVERNMENT_ORGANISATION_PROFILE"),

    GOVT_HO("GOVT-HO","Government Organisation Home Office",
                    "GOVT_HO_PROFILE","GOVERNMENT_ORGANISATION_PROFILE"),

    GOVT_HMRC("GOVT-HMRC","Government Organisation HMRC",
                      "GOVT_HMRC_PROFILE","GOVERNMENT_ORGANISATION_PROFILE"),

    GOVT_CICA("GOVT-CICA","Government Organisation CICA",
                      "GOVT_CICA_PROFILE","GOVERNMENT_ORGANISATION_PROFILE"),

    GOVT_CAFCASS_CYMRU("GOVT-CAFCASS-CYMRU","Government Organisation CAFCASS CYMRU",
                               "GOVT_CAFCASS_CYMRU_PROFILE","GOVERNMENT_ORGANISATION_PROFILE"),

    GOVT_IBCA("GOVT-IBCA","Government Organisation IBCA",
                      "GOVT_IBCA_PROFILE","GOVERNMENT_ORGANISATION_PROFILE");

     */

    private static final Map<String, List<String>> ORG_TYPE_TO_ORG_PROFILE_IDS = Map.ofEntries(
            new SimpleEntry<>(OrganisationTypeConstants.SOLICITOR_ORG,
                    List.of(OrganisationProfileIdConstants.SOLICITOR_PROFILE)),
            new SimpleEntry<>(OrganisationTypeConstants.LOCAL_AUTHORITY_ORG,
                    List.of(OrganisationProfileIdConstants.SOLICITOR_PROFILE)),
            new SimpleEntry<>(OrganisationTypeConstants.PROBATE_PRACTITIONER,
                    List.of(OrganisationProfileIdConstants.SOLICITOR_PROFILE)),
            new SimpleEntry<>(OrganisationTypeConstants.OTHER_ORG,
                    List.of(OrganisationProfileIdConstants.SOLICITOR_PROFILE)),
            new SimpleEntry<>(OrganisationTypeConstants.BARRISTER,
                    List.of(OrganisationProfileIdConstants.SOLICITOR_PROFILE)),
            new SimpleEntry<>(OrganisationTypeConstants.OGD_DWP_ORG,
                    List.of(OrganisationProfileIdConstants.OGD_DWP_PROFILE)),
            new SimpleEntry<>(OrganisationTypeConstants.OGD_HO_ORG,
                    List.of(OrganisationProfileIdConstants.OGD_HO_PROFILE)),
            new SimpleEntry<>(OrganisationTypeConstants.OGD_OTHER_ORG, List.of(
                    OrganisationProfileIdConstants.SOLICITOR_PROFILE)),
            new SimpleEntry<>(OrganisationTypeConstants.OGD_HMRC_ORG,
                    List.of(OrganisationProfileIdConstants.OGD_HMRC_PROFILE)),
            new SimpleEntry<>(OrganisationTypeConstants.OGD_CICA_ORG,
                    List.of(OrganisationProfileIdConstants.OGD_CICA_PROFILE)),
            new SimpleEntry<>(OrganisationTypeConstants.OGD_CAFCASS_CYMRU_ORG,
                    List.of(OrganisationProfileIdConstants.OGD_CAFCASS_PROFILE_CYMRU)),
            new SimpleEntry<>(OrganisationTypeConstants.OGD_CAFCASS_ENGLAND_ORG,
                    List.of(OrganisationProfileIdConstants.OGD_CAFCASS_PROFILE_ENGLAND))
    );

    public static List<String> toProfileIds(String orgType) {
        if (orgType == null) {
            return List.of();
        }
        return ORG_TYPE_TO_ORG_PROFILE_IDS.getOrDefault(orgType, List.of());
    }

    public static List<String> toOrgTypes(String profileId) {
        if (profileId == null) {
            return List.of();
        }
        return ORG_TYPE_TO_ORG_PROFILE_IDS.entrySet()
                .stream()
                .filter(e -> e.getValue() != null && e.getValue().contains(profileId))
                .map(Map.Entry::getKey)
                .toList();
    }
}