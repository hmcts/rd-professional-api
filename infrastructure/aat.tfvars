sku_name = "GP_Gen5_4"
sku_capacity = "4"
pgsql_server_configuration = [
  {
    name  = "azure.extensions"
    value = "PLPGSQL"
  },
  {
    name  = "backslash_quote"
    value = "ON"
  }]