package uk.gov.hmcts.reform.professionalapi.util;

public enum ProfileOrgTypeUtility {

    private final String orgType;
    private final String description;
    private final Set<String> profileIds;

    ProfileOrgTypeUtility(String orgType, String description, String... profileIds) {
        this.orgType = orgType;
        this.description = description;
        this.profileIds = Set.of(profileIds);
    }

    GOVT("GOVT","Government Organisation",
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

    public String getOrgType() {
        return orgType;
    }

    public Set<String> getProfileIds() {
        return profileIds;
    }

    private static final Map<String, OrgType> BY_ORG_TYPE =
            Arrays.stream(values())
                    .collect(Collectors.toMap(OrgType::getOrgType, o -> o));

    private static final Map<String, Set<OrgType>> BY_PROFILE_ID =
            Arrays.stream(values())
                    .flatMap(o -> o.profileIds.stream()
                            .map(p -> Map.entry(p, o)))
                    .collect(Collectors.groupingBy(
                            Map.Entry::getKey,
                            Collectors.mapping(Map.Entry::getValue, Collectors.toSet())
                    ));

    public static Optional<OrgType> fromOrgType(String orgType) {
        return Optional.ofNullable(BY_ORG_TYPE.get(orgType));
    }

    public static Set<String> toProfileIds(String orgType) {
        return fromOrgType(orgType)
                .map(OrgType::getProfileIds)
                .orElse(Set.of());
    }

    public static Set<String> toOrgTypes(String profileId) {
        return BY_PROFILE_ID.getOrDefault(profileId, Set.of())
                .stream()
                .map(OrgType::getOrgType)
                .collect(Collectors.toSet());
    }
}