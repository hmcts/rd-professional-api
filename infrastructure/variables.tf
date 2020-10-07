variable "product" {
  type = string
}

variable "raw_product" {
  type    = string
  default = "rd" // jenkins-library overrides product for PRs and adds e.g. pr-123-ia
}

variable "component" {
  type = string
}

variable "location" {
  type    = string
  default = "UK South"
}

variable "env" {
  type = string
}

variable "subscription" {
  type = string
}

# variable "ilbIp" {
#   type = string
# }

variable "common_tags" {
  type = map(string)
}

# variable "appinsights_instrumentation_key" {
#   type    = string
#   default = ""
# }

# variable "root_logging_level" {
#   type    = string
#   default = "INFO"
# }

# variable "log_level_spring_web" {
#   type    = string
#   default = "INFO"
# }

# variable "log_level_rd" {
#   type    = string
#   default = "INFO"
# }

variable "team_name" {
  type    = string
  default = "RD"
}

# variable "managed_identity_object_id" {
#   type    = string
#   default = ""
# }