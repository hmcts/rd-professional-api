output "resourceGroup" {
  value = azurerm_resource_group.rg.name
}

output "username" {
  value = module.db-professional-ref-data-v15.username
}