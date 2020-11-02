locals {
  preview_vault_name      = join("-", [var.raw_product, "aat"])
  non_preview_vault_name  = join("-", [var.raw_product, var.env])
  key_vault_name          = var.env == "preview" || var.env == "spreview" ? local.preview_vault_name : local.non_preview_vault_name
}

data "azurerm_key_vault" "rd_key_vault" {
  name                = local.key_vault_name
  resource_group_name = local.key_vault_name
}

# resource "azurerm_key_vault_secret" "POSTGRES_HOST" {
#   provider      = azurerm.azure-1
#   name          = join("-", [var.component, "POSTGRES-HOST"])
#   value         = module.db-professional-ref-data.host_name
#   key_vault_id  = data.azurerm_key_vault.rd_key_vault.id
# }

# resource "azurerm_key_vault_secret" "POSTGRES_PORT" {
#   provider      = azurerm.azure-1
#   name          = join("-", [var.component, "POSTGRES-PORT"])
#   value         = "5432"
#   key_vault_id  = data.azurerm_key_vault.rd_key_vault.id
# }

# resource "azurerm_key_vault_secret" "POSTGRES-USER" {
#   provider      = azurerm.azure-1
#   name          = join("-", [var.component, "POSTGRES-USER"])
#   value         = module.db-professional-ref-data.user_name
#   key_vault_id  = data.azurerm_key_vault.rd_key_vault.id
# }

# resource "azurerm_key_vault_secret" "POSTGRES-PASS" {
#   provider      = azurerm.azure-1
#   name          = join("-", [var.component, "POSTGRES-PASS"])
#   value         = module.db-professional-ref-data.postgresql_password
#   key_vault_id  = data.azurerm_key_vault.rd_key_vault.id
# }

# resource "azurerm_key_vault_secret" "POSTGRES_DATABASE" {
#   provider      = azurerm.azure-1
#   name          = join("-", [var.component, "POSTGRES-DATABASE"])
#   value         = module.db-professional-ref-data.postgresql_database
#   key_vault_id  = data.azurerm_key_vault.rd_key_vault.id
# }

resource "azurerm_resource_group" "rg" {
  name      = join("-", [var.product, var.component, var.env])
  location  = var.location
  tags      = {
    "Deployment Environment"  = var.env
    "Team Name"               = var.team_name
    "lastUpdated"             = timestamp()
  }
}

module "db-professional-ref-data" {
  source          = "git@github.com:hmcts/cnp-module-postgres?ref=master"
  product         = join("-", [var.product, var.component, "postgres-db"])
  location        = var.location
  subscription    = var.subscription
  env             = var.env
  postgresql_user = "dbrefdata"
  database_name   = "dbrefdata"
  common_tags     = var.common_tags
}