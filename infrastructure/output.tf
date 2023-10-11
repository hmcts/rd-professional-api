output "resourceGroup" {
  value = azurerm_resource_group.rg.name
}

output "username" {
  value = "${var.pgsql_admin_username}-${var.env}"
}