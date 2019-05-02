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
  s2s_vault_url = "https://s2s-${local.local_env}.vault.azure.net/"
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

data "azurerm_key_vault_secret" "s2s_microservice" {
  name = "s2s-microservice"
  vault_uri = "${data.azurerm_key_vault.rd_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "s2s_url" {
  name = "s2s-url"
  vault_uri = "${data.azurerm_key_vault.rd_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "s2s_secret" {
  name = "microservicekey-rd-professional-api"
  vault_uri = "${local.s2s_vault_url}"
}

module "db-professional-ref-data" {
  source = "git@github.com:hmcts/cnp-module-postgres?ref=master"
  product = "${var.product}-${var.component}-postgres-db"
  location = "${var.location}"
  env = "${var.env}"
  postgresql_user = "${var.postgresql_user}"
  database_name = "${var.database_name}"
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
    S2S_SECRET                 = "${data.azurerm_key_vault_secret.s2s_secret.value}"

    S2S_URL = "${local.s2s_url}"

    ROOT_LOGGING_LEVEL = "${var.root_logging_level}"
    LOG_LEVEL_SPRING_WEB = "${var.log_level_spring_web}"
    LOG_LEVEL_RD = "${var.log_level_rd}"
    EXCEPTION_LENGTH = 100
  }
}
