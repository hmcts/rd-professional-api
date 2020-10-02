output "resourceGroup" {
  value = azurerm_resource_group.rg.name
}

output "vaultName" {
  value = local.key_vault_name
}