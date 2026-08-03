terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.6"
    }
  }

  # Optional remote state — copy backend.hcl.example and:
  #   terraform init -backend-config=backend.hcl
  # backend "s3" {}
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = merge(
      {
        Project     = var.project_name
        ManagedBy   = "terraform"
        Environment = var.environment
      },
      var.extra_tags
    )
  }
}
