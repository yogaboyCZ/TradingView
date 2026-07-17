package cz.yogaboy.tv.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.yogaboy.feature.home.presentation.HomeEffect
import cz.yogaboy.feature.home.presentation.HomeScreen
import cz.yogaboy.feature.home.presentation.HomeViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeRoute(
    onNavigateToDetail: (String) -> Unit,
    wideLayout: Boolean = false,
    supportingPane: Boolean = false,
    selectedTicker: String? = null,
    drawBackground: Boolean = true,
) {
    val viewModel: HomeViewModel = koinViewModel()
    val state = viewModel.state.collectAsStateWithLifecycle().value

    LaunchedEffect(viewModel, onNavigateToDetail) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HomeEffect.NavigateToDetail -> onNavigateToDetail(effect.ticker)
            }
        }
    }

    HomeScreen(
        state = state,
        onEvent = viewModel::handle,
        wideLayout = wideLayout,
        supportingPane = supportingPane,
        selectedTicker = selectedTicker,
        drawBackground = drawBackground,
    )
}
