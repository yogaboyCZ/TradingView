package cz.yogaboy.feature.home.di

import cz.yogaboy.feature.home.presentation.HomeViewModel
import org.koin.dsl.module
import org.koin.plugin.module.dsl.viewModel

val homeModule = module {
    viewModel<HomeViewModel>()
}
