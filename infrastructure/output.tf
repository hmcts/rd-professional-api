output "microserviceName" {
  value = "${var.component}"
}

output "resourceGroup" {
  value = "${azurerm_resource_group.rg.name}"
}

output "appServicePlan" {
  value = "${local.app_service_plan}"
}
