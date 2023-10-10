output "resourceGroup" {
  value = azurerm_resource_group.rg.name
}

output "username" {
  value = join("@", ["dbrefdata", join("-", [var.product-V15, var.component-V15])])
}