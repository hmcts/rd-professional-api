output "microserviceName" {
  value = "${var.component}"
}

output "resourceGroup" {
  value = "${azurerm_resource_group.rg.name}"
}

output "appServicePlan" {
  value = "${local.app_service_plan}"
}

output "vaultName" {
  value = "${local.key_vault_name}"
}

output "vaultUri" {
  value = "${local.s2s_vault_uri}"
}
