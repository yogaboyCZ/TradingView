package cz.yogaboy.tv.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel
import cz.yogaboy.feature.home.presentation.*

@Composable
fun HomeEntryScreen(
    onNavigateToDetail: (String) -> Unit,
    supportingPane: Boolean = false,
) {
    val vm: HomeViewModel = koinViewModel()
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) {
        vm.effects.collect { effect ->
            when (effect) {
                is HomeEffect.NavigateToDetail -> onNavigateToDetail(effect.ticker)
            }
        }
    }

    HomeScreen(
        state = state,
        onEvent = { vm.handle(it) },
        supportingPane = supportingPane,
    )
}
