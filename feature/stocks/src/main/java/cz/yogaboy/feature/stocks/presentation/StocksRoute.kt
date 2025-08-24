package cz.yogaboy.feature.stocks.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import org.koin.core.context.GlobalContext
import org.koin.core.parameter.parametersOf

@Composable
fun StocksRoute(
    ticker: String,
    onBackClick: () -> Unit,
) {
    val owner = LocalViewModelStoreOwner.current!!
    val koin = GlobalContext.get()
    val factory = remember(ticker) {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                @Suppress("UNCHECKED_CAST")
                return koin.get<StocksViewModel> { parametersOf(ticker) } as T
            }
        }
    }
    val vm = remember(owner, ticker) {
        ViewModelProvider(owner, factory).get(StocksViewModel::class.java)
    }
    val state by vm.state.collectAsStateWithLifecycle()
    StocksScreen(
        state = state,
        onClick = vm::handle,
        onBackClick = onBackClick
    )
}
