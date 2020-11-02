provider "azurerm" {
  features {}
}

terraform {
  backend "azurerm" {}

  required_providers {
    azurerm = {
      source = "hashicorp/azurerm"
      version = "~> 2.25"
    }
  }
}

provider "azurerm" {
  alias = "azure-1"
  # version = "=2.20.0"
  features {}
}