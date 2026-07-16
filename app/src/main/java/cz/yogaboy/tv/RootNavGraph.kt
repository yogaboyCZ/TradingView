package cz.yogaboy.app

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import cz.yogaboy.tv.nav.HomeDest
import cz.yogaboy.tv.nav.HomeEntryScreen

@Composable
fun RootNavGraph(initialTicker: String? = null) {
    val backStack = rememberNavBackStack(HomeDest)

    NavDisplay(backStack, entryProvider = entryProvider {
        entry<HomeDest> {
            HomeEntryScreen(initialTicker = initialTicker)
        }
    })
}
