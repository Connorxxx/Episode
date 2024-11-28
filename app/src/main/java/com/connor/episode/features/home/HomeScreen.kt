package com.connor.episode.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.connor.episode.core.utils.navigateTopTo
import com.connor.episode.domain.model.uimodel.HomeAction
import com.connor.episode.features.HOME
import com.connor.episode.features.HomeRoute
import com.connor.episode.features.serial.SerialPortScreen
import com.connor.episode.features.serial.SerialPortViewModel
import com.connor.episode.features.tcp.TcpScreen
import com.connor.episode.features.udp.UdpScreen
import com.connor.episode.features.websocket.WebSocketScreen
import com.connor.episode.ui.theme.EpisodeTheme

@Composable
fun HomeScreen(modifier: Modifier = Modifier, vm: HomeViewModel = hiltViewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val serialPortViewModel: SerialPortViewModel = hiltViewModel()
    LaunchedEffect(state.currentRoute) {
        if (state.currentRoute != navController.currentDestination?.route)
            navController.navigateTopTo(state.currentRoute)
    }
    Home(
        modifier = modifier,
        currentRoute = state.currentRoute,
        routes = state.routes,
        onAction = vm::onAction
    ) {
        NavHost(navController = navController, HomeRoute.SerialPort.route) {
            composable(HomeRoute.SerialPort.route) {
                SerialPortScreen(serialPortViewModel)
            }
            composable(HomeRoute.Tcp.route) {
                TcpScreen()
            }
            composable(HomeRoute.Udp.route) {
                UdpScreen()
            }
            composable(HomeRoute.WebSocket.route) {
                WebSocketScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Home(
    modifier: Modifier = Modifier,
    currentRoute: String = HomeRoute.startDestination,
    routes: List<HomeRoute> = HomeRoute.routes,
    onAction: (HomeAction) -> Unit = {},
    navHost: @Composable ColumnScope.() -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PrimaryTabRow(
            selectedTabIndex = HomeRoute.routes.indexOfFirst { it.route == currentRoute },
        ) {
            routes.forEachIndexed { _, route ->
                Tab(
                    selected = currentRoute == route.route,
                    onClick = {
                        onAction(HomeAction.RouteChange(route.route))
                    },
                    text = {
                        Text(
                            text = route.title,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                )
            }
        }
        navHost()
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    EpisodeTheme {
        Home()
    }
}

