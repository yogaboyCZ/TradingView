package cz.yogaboy.core.network

import org.koin.core.qualifier.named
import org.koin.dsl.module

val coreNetworkModule = module {
    single(named("alphaApiKey")) { NetworkConfig.apiKey }
    single(named("alphaBaseUrl")) { NetworkConfig.baseUrl }
}
