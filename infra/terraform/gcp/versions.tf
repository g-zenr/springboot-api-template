terraform {
  required_version = ">= 1.5.0"

  required_providers {
    # Add your provider, e.g. azurerm, google, digitalocean
    # azurerm = { source = "hashicorp/azurerm", version = "~> 3.0" }
  }

  # Optional remote state â€” copy backend.hcl.example
  # backend "azurerm" {}
  # backend "gcs" {}
}

# provider "azurerm" { features {} }
