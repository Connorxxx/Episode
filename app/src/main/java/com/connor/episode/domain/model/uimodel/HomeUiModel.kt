package com.connor.episode.domain.model.uimodel

import com.connor.episode.features.HomeRoute

data class HomeState(
    val currentRoute: String = HomeRoute.startDestination,
    val routes: List<HomeRoute> = HomeRoute.routes
)

sealed interface HomeAction {
    data class RouteChange(val route: String) : HomeAction
}
