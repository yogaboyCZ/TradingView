package cz.yogaboy.feature.home.navigation

interface HomeEntry {
    val route: String
}

object HomeEntryImpl : HomeEntry {
    override val route = "home"
}
