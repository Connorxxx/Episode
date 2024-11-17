package com.connor.episode.models

import com.connor.episode.ui.HomeRoute

data class HomeState(
    val currentRoute: String = HomeRoute.startDestination,
    val routes: List<HomeRoute> = HomeRoute.routes
)

sealed interface HomeAction {
    data class RouteChange(val route: String) : HomeAction
}
