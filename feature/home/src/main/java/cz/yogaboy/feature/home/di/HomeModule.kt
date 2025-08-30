package cz.yogaboy.feature.home.di

import cz.yogaboy.feature.home.domain.SearchSymbolsUseCase
import cz.yogaboy.feature.home.presentation.HomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val homeModule = module {
    factory<SearchSymbolsUseCase> { SearchSymbolsUseCase { emptyList() } }
    viewModel { HomeViewModel(get()) }
}
