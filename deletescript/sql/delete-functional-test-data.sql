begin;

create temp table tmp_prd_user_ids on
commit drop as
select
    pu.id
from
    dbrefdata.professional_user pu
where
    pu.email_address like '%@prdfunctestuser.com'
  and pu.created <= CURRENT_TIMESTAMP - interval '1 hour';

create temp table tmp_org_ids on
commit drop as
select
    o.id
from
    dbrefdata.organisation o
        join dbrefdata.professional_user pu on
        o.id = pu.organisation_id
where
    pu.email_address like '%@prdfunctestuser.com'
  and pu.created <= CURRENT_TIMESTAMP - interval '1 hour';

create
temp table tmp_contacts_ids on
commit drop
as
select
    c.id
from
    dbrefdata.contact_information c
        join tmp_org_ids t
             on
                 c.organisation_id = t.id;
-- BEFORE deleting counts
select
    'user_attribute' as table_name,
    count(*) as before_delete_count
from
    dbrefdata.user_attribute
where
    professional_user_id in (
        select
            id
        from
            tmp_prd_user_ids)
union all
select
    'user_account_map',
    count(*)
from
    dbrefdata.user_account_map
where
    professional_user_id in (
        select
            id
        from
            tmp_prd_user_ids)
union all
select
    'professional_user',
    count(*)
from
    dbrefdata.professional_user
where
    id in (
        select
            id
        from
            tmp_prd_user_ids)
union all
select
    'payment_account',
    count(*)
from
    dbrefdata.payment_account
where
    organisation_id in (
        select
            id
        from
            tmp_org_ids)
union all
select
    'dx_address',
    count(*)
from
    dbrefdata.dx_address
where
    contact_information_id in (
        select
            id
        from
            tmp_contacts_ids)
union all
select
    'contact_information',
    count(*)
from
    dbrefdata.contact_information
where
    id in (
        select
            id
        from
            tmp_contacts_ids)
union all
select
    'organisation_mfa_status',
    count(*)
from
    dbrefdata.organisation_mfa_status
where
    organisation_id in (
        select
            id
        from
            tmp_org_ids)
union all
select
    'org_attributes',
    count(*)
from
    dbrefdata.org_attributes
where
    organisation_id in (
        select
            id
        from
            tmp_org_ids)
union all
select
    'organisation',
    count(*)
from
    dbrefdata.organisation
where
    id in (
        select
            id
        from
            tmp_org_ids);
-- Deletes and prints the actual rows removed per table
with del_user_attribute as (
delete
from
    dbrefdata.user_attribute
where
    professional_user_id in (
        select
            id
        from
            tmp_prd_user_ids) returning 1),
    del_user_account_map as (
delete
from
    dbrefdata.user_account_map
where
    professional_user_id in (
    select
    id
    from
    tmp_prd_user_ids) returning 1),
    del_professional_user as (
delete
from
    dbrefdata.professional_user
where
    id in (
    select
    id
    from
    tmp_prd_user_ids) returning 1),
    del_payment_account as (
delete
from
    dbrefdata.payment_account
where
    organisation_id in (
    select
    id
    from
    tmp_org_ids) returning 1),
    del_dx_address as (
delete
from
    dbrefdata.dx_address
where
    contact_information_id in (
    select
    id
    from
    tmp_contacts_ids) returning 1),
    del_contact_information as (
delete
from
    dbrefdata.contact_information
where
    id in (
    select
    id
    from
    tmp_contacts_ids) returning 1),
    del_organisation_mfa_status as (
delete
from
    dbrefdata.organisation_mfa_status
where
    organisation_id in (
    select
    id
    from
    tmp_org_ids) returning 1),
    del_org_attributes as (
delete
from
    dbrefdata.org_attributes
where
    organisation_id in (
    select
    id
    from
    tmp_org_ids) returning 1),
    del_organisation as (
delete
from
    dbrefdata.organisation
where
    id in (
    select
    id
    from
    tmp_org_ids) returning 1)
select
    (
        select
            count(*)
        from
            del_user_attribute) as deleted_user_attribute,
    (
        select
            count(*)
        from
            del_user_account_map) as deleted_user_account_map,
    (
        select
            count(*)
        from
            del_professional_user) as deleted_professional_user,
    (
        select
            count(*)
        from
            del_payment_account) as deleted_payment_account,
    (
        select
            count(*)
        from
            del_dx_address) as deleted_dx_address,
    (
        select
            count(*)
        from
            del_contact_information) as deleted_contact_information,
    (
        select
            count(*)
        from
            del_organisation_mfa_status) as deleted_organisation_mfa_status,
    (
        select
            count(*)
        from
            del_org_attributes) as deleted_org_attributes,
    (
        select
            count(*)
        from
            del_organisation) as deleted_organisation;
-- AFTER deleting counts
select
    'user_attribute' as table_name,
    count(*) as after_delete_count
from
    dbrefdata.user_attribute
where
    professional_user_id in (
        select
            id
        from
            tmp_prd_user_ids)
union all
select
    'user_account_map',
    count(*)
from
    dbrefdata.user_account_map
where
    professional_user_id in (
        select
            id
        from
            tmp_prd_user_ids)
union all
select
    'professional_user',
    count(*)
from
    dbrefdata.professional_user
where
    id in (
        select
            id
        from
            tmp_prd_user_ids)
union all
select
    'payment_account',
    count(*)
from
    dbrefdata.payment_account
where
    organisation_id in (
        select
            id
        from
            tmp_org_ids)
union all
select
    'dx_address',
    count(*)
from
    dbrefdata.dx_address
where
    contact_information_id in (
        select
            id
        from
            tmp_contacts_ids)
union all
select
    'contact_information',
    count(*)
from
    dbrefdata.contact_information
where
    id in (
        select
            id
        from
            tmp_contacts_ids)
union all
select
    'organisation_mfa_status',
    count(*)
from
    dbrefdata.organisation_mfa_status
where
    organisation_id in (
        select
            id
        from
            tmp_org_ids)
union all
select
    'org_attributes',
    count(*)
from
    dbrefdata.org_attributes
where
    organisation_id in (
        select
            id
        from
            tmp_org_ids)
union all
select
    'organisation',
    count(*)
from
    dbrefdata.organisation
where
    id in (
        select
            id
        from
            tmp_org_ids);

commit;
