output "resourceGroup" {
  value = azurerm_resource_group.rg.name
}

output "username" {
  value = azurerm_postgresql_flexible_server.pgsql_server.administrator_login
}

