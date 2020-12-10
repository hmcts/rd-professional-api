def secrets = [
    's2s-${env}': [
    secret('microservicekey-rd-professional-api', 'S2S_SECRET')
    ]
]
withPipeline() {
    loadVaultSecrets(secrets)
}