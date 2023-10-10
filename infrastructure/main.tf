locals {
  tags = (merge(
    var.common_tags,
    tomap({
      "Team Contact" = var.team_contact
      "Destroy Me"   = var.destroy_me
    })
  ))
  preview_vault_name      = join("-", [var.raw_product, "aat"])
  non_preview_vault_name  = join("-", [var.raw_product, var.env])
  key_vault_name          = var.env == "preview" || var.env == "spreview" ? local.preview_vault_name : local.non_preview_vault_name

  s2s_rg_prefix               = "rpe-service-auth-provider"
  s2s_key_vault_name          = var.env == "preview" || var.env == "spreview" ? join("-", ["s2s", "aat"]) : join("-", ["s2s", var.env])
  s2s_vault_resource_group    = var.env == "preview" || var.env == "spreview" ? join("-", [local.s2s_rg_prefix, "aat"]) : join("-", [local.s2s_rg_prefix, var.env])
}

data "azurerm_key_vault" "rd_key_vault" {
  name                = local.key_vault_name
  resource_group_name = local.key_vault_name
}

data "azurerm_key_vault" "s2s_key_vault" {
  name                = local.s2s_key_vault_name
  resource_group_name = local.s2s_vault_resource_group
}

data "azurerm_key_vault_secret" "s2s_secret" {
  name          = "microservicekey-rd-professional-api"
  key_vault_id  = data.azurerm_key_vault.s2s_key_vault.id
}

resource "azurerm_key_vault_secret" "professional_s2s_secret" {
  name          = "professional-api-s2s-secret"
  value         = data.azurerm_key_vault_secret.s2s_secret.value
  key_vault_id  = data.azurerm_key_vault.rd_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES_HOST" {
  name          = join("-", [var.component, "POSTGRES-HOST"])
  value         = module.db-professional-ref-data-v11.host_name
  key_vault_id  = data.azurerm_key_vault.rd_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES_PORT" {
  name          = join("-", [var.component, "POSTGRES-PORT"])
  value         = "5432"
  key_vault_id  = data.azurerm_key_vault.rd_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES-USER" {
  name          = join("-", [var.component, "POSTGRES-USER"])
  value         = module.db-professional-ref-data-v11.user_name
  key_vault_id  = data.azurerm_key_vault.rd_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS" {
  name          = join("-", [var.component, "POSTGRES-PASS"])
  value         = module.db-professional-ref-data-v11.postgresql_password
  key_vault_id  = data.azurerm_key_vault.rd_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES_DATABASE" {
  name          = join("-", [var.component, "POSTGRES-DATABASE"])
  value         = module.db-professional-ref-data-v11.postgresql_database
  key_vault_id  = data.azurerm_key_vault.rd_key_vault.id
}

resource "azurerm_resource_group" "rg" {
  name      = join("-", [var.product, var.component, var.env])
  location  = var.location
  tags      = {
    "Deployment Environment"  = var.env
    "Team Name"               = var.team_name
    "lastUpdated"             = timestamp()
  }
}

module "db-professional-ref-data-v11" {
  source             = "git@github.com:hmcts/cnp-module-postgres?ref=master"
  product            = var.product
  component          = var.component
  name               = join("-", [var.product, var.component, "postgres-db", "v11"])
  location           = var.location
  subscription       = var.subscription
  env                = var.env
  postgresql_user    = "dbrefdata"
  database_name      = "dbrefdata"
  common_tags        = var.common_tags
  postgresql_version = "11"
}


# Create the database server V15
# Name and resource group name will be defaults (<product>-<component>-<env> and <product>-<component>-data-<env> respectively)
module "db-professional-ref-data-v15" {
  source = "git@github.com:hmcts/terraform-module-postgresql-flexible?ref=master"

  providers = {
    azurerm.postgres_network = azurerm.postgres_network
  }

  admin_user_object_id = var.jenkins_AAD_objectId
  business_area        = "cft"
  common_tags          = var.common_tags
  component            = var.component-V15
  env                  = var.env
  pgsql_databases = [
    {
      name = "dbrefdata"
    }
  ]
  pgsql_version        = "15"
  product              = var.product-V15
  name               = join("-", [var.product-V15, var.component-V15])
}

resource "azurerm_postgresql_flexible_server" "pgsql_server" {
  administrator_login = join("@", ["dbrefdata", join("-", [var.product-V15, var.component-V15])])
}

resource "azurerm_key_vault_secret" "POSTGRES-USER-V15" {
  name          = join("-", [var.component, "POSTGRES-USER-V15"])
  value         = join("@", ["dbrefdata", join("-", [var.product-V15, var.component-V15])])
  key_vault_id  = data.azurerm_key_vault.rd_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES_HOST-V15" {
  name          = join("-", [var.component, "POSTGRES-HOST-V15"])
  value         = module.db-professional-ref-data-v15.fqdn
  key_vault_id  = data.azurerm_key_vault.rd_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS-V15" {
  name          = join("-", [var.component, "POSTGRES-PASS-V15"])
  value         = module.db-professional-ref-data-v15.password
  key_vault_id  = data.azurerm_key_vault.rd_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES_DATABASE-V15" {
  name          = join("-", [var.component, "POSTGRES-DATABASE-V15"])
  value         = "dbrefdata"
  key_vault_id  = data.azurerm_key_vault.rd_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES_PORT-V15" {
  name          = join("-", [var.component, "POSTGRES-PORT-V15"])
  value         = "5432"
  key_vault_id  = data.azurerm_key_vault.rd_key_vault.id
}
