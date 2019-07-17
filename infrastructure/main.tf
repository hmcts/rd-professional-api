# Temporary fix for template API version error on deployment
provider "azurerm" {
  version = "1.22.0"
}

locals {
  local_env = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "aat" : "saat" : var.env}"
  preview_app_service_plan = "${var.product}-${var.component}-${var.env}"
  non_preview_app_service_plan = "${var.product}-${var.env}"
  app_service_plan = "${var.env == "preview" || var.env == "spreview" ? local.preview_app_service_plan : local.non_preview_app_service_plan}"

  preview_vault_name = "${var.raw_product}-aat"
  non_preview_vault_name = "${var.raw_product}-${var.env}"
  key_vault_name = "${var.env == "preview" || var.env == "spreview" ? local.preview_vault_name : local.non_preview_vault_name}"

  s2s_url = "http://rpe-service-auth-provider-${local.local_env}.service.core-compute-${local.local_env}.internal"
  s2s_vault_name = "s2s-${local.local_env}"
  s2s_vault_uri = "https://s2s-${local.local_env}.vault.azure.net/"
  idam_url = "https://idam-api.${local.local_env}.platform.hmcts.net"
  USER_PROFILE_URL = "http://rd-user-profile-api-${local.local_env}.service.core-compute-${local.local_env}.internal"
}

resource "azurerm_resource_group" "rg" {
  name = "${var.product}-${var.component}-${var.env}"
  location = "${var.location}"
  tags = "${merge(var.common_tags, map("lastUpdated", "${timestamp()}"))}"
}

data "azurerm_key_vault" "rd_key_vault" {
  name = "${local.key_vault_name}"
  resource_group_name = "${local.key_vault_name}"
}

data "azurerm_key_vault" "s2s_key_vault" {
  name = "s2s-${local.local_env}"
  resource_group_name = "rpe-service-auth-provider-${local.local_env}"
}

data "azurerm_key_vault_secret" "s2s_microservice" {
  name = "s2s-microservice"
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"
}

data "azurerm_key_vault_secret" "s2s_url" {
  name = "s2s-url"
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"
}

data "azurerm_key_vault_secret" "idam_url" {
  name = "idam-url"
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"
}

data "azurerm_key_vault_secret" "USER_PROFILE_URL" {
 name = "USER-PROFILE-URL"
 vault_user_profile_uri = "${data.azurerm_key_vault.rd_key_vault.vault_user_profile_uri}"
}

data "azurerm_key_vault_secret" "s2s_secret" {
  name = "microservicekey-rd-professional-api"
  key_vault_id = "${data.azurerm_key_vault.s2s_key_vault.id}"
}

data "azurerm_key_vault_secret" "oauth2_redirect_uri" {
  name = "OAUTH2-REDIRECT-URI"
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"
}

data "azurerm_key_vault_secret" "oauth2_client_id" {
  name = "OAUTH2-CLIENT-ID"
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"
}

data "azurerm_key_vault_secret" "oauth2_client_secret" {
  name = "OAUTH2-CLIENT-SECRET"
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"
}

resource "azurerm_key_vault_secret" "POSTGRES-USER" {
  name      = "${var.component}-POSTGRES-USER"
  value     = "${module.db-professional-ref-data.user_name}"
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS" {
  name      = "${var.component}-POSTGRES-PASS"
  value     = "${module.db-professional-ref-data.postgresql_password}"
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"
}

resource "azurerm_key_vault_secret" "POSTGRES_HOST" {
  name      = "${var.component}-POSTGRES-HOST"
  value     = "${module.db-professional-ref-data.host_name}"
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"
}

resource "azurerm_key_vault_secret" "POSTGRES_PORT" {
  name      = "${var.component}-POSTGRES-PORT"
  value     = "5432"
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"
}

resource "azurerm_key_vault_secret" "POSTGRES_DATABASE" {
  name      = "${var.component}-POSTGRES-DATABASE"
  value     = "${module.db-professional-ref-data.postgresql_database}"
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"
}

resource "azurerm_resource_group" "rg" {
  name = "${var.product}-${var.env}"
  location = "${var.location}"
  tags {
    "Deployment Environment" = "${var.env}"
    "Team Name" = "${var.team_name}"
    "Team Contact" = "${var.team_contact}"
    "Destroy Me" = "${var.destroy_me}"
  }
}

module "db-professional-ref-data" {
  source = "git@github.com:hmcts/cnp-module-postgres?ref=master"
  product = "${var.product}-${var.component}-postgres-db"
  location = "${var.location}"
  subscription = "${var.subscription}"
  env = "${var.env}"
  postgresql_user = "dbrefdata"
  database_name = "dbrefdata"
  common_tags = "${var.common_tags}"
}

module "rd_professional_api" {
  source = "git@github.com:hmcts/cnp-module-webapp?ref=master"
  product = "${var.product}-${var.component}"
  location = "${var.location}"
  env = "${var.env}"
  ilbIp = "${var.ilbIp}"
  resource_group_name = "${azurerm_resource_group.rg.name}"
  subscription = "${var.subscription}"
  capacity = "${var.capacity}"
  instance_size = "${var.instance_size}"
  common_tags = "${merge(var.common_tags, map("lastUpdated", "${timestamp()}"))}"
  appinsights_instrumentation_key = "${var.appinsights_instrumentation_key}"
  asp_name = "${local.app_service_plan}"
  asp_rg = "${local.app_service_plan}"

  app_settings = {
    LOGBACK_REQUIRE_ALERT_LEVEL = false
    LOGBACK_REQUIRE_ERROR_CODE = false

    POSTGRES_HOST = "${module.db-professional-ref-data.host_name}"
    POSTGRES_PORT = "${module.db-professional-ref-data.postgresql_listen_port}"
    POSTGRES_DATABASE = "${module.db-professional-ref-data.postgresql_database}"
    POSTGRES_USER = "${module.db-professional-ref-data.user_name}"
    POSTGRES_USERNAME = "${module.db-professional-ref-data.user_name}"
    POSTGRES_PASSWORD = "${module.db-professional-ref-data.postgresql_password}"
    POSTGRES_CONNECTION_OPTIONS = "?"

    S2S_URL = "${local.s2s_url}"
    IDAM_URL = "${data.azurerm_key_vault_secret.idam_url.value}"
    USER_PROFILE_URL = "${data.azurerm_key_vault_secret.USER_PROFILE_URL.value}"

    OAUTH2_REDIRECT_URI = "${data.azurerm_key_vault_secret.oauth2_redirect_uri.value}"
    OAUTH2_CLIENT_ID = "${data.azurerm_key_vault_secret.oauth2_client_id.value}"
    OAUTH2_CLIENT_SECRET = "${data.azurerm_key_vault_secret.oauth2_client_secret.value}"

    ROOT_LOGGING_LEVEL = "${var.root_logging_level}"
    LOG_LEVEL_SPRING_WEB = "${var.log_level_spring_web}"
    LOG_LEVEL_RD = "${var.log_level_rd}"
    EXCEPTION_LENGTH = 100
  }
}
