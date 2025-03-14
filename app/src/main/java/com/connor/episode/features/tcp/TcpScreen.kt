package com.connor.episode.features.tcp

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.connor.episode.domain.model.business.Message
import com.connor.episode.domain.model.business.NetResult
import com.connor.episode.domain.model.uimodel.NetAction
import com.connor.episode.domain.model.uimodel.NetState
import com.connor.episode.features.tcp.components.TCPSettingDialog
import com.connor.episode.features.common.ui.common.ChatMessageLazyColumn
import com.connor.episode.features.common.ui.common.MessageBottomBar
import com.connor.episode.features.common.ui.common.PreviewMessageLazyColumn
import com.connor.episode.features.common.ui.common.TopBar
import com.connor.episode.features.common.ui.theme.EpisodeTheme

@Composable
fun TcpScreen(vm: TCPViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val pagingMessages = vm.messagePagingFlow.collectAsLazyPagingItems()
    Tcp(
        state = state,
        pagingMessages,
        onAction = vm::onAction
    )
    if (state.isShowSettingDialog) TCPSettingDialog(
        state = state,
        onAction = vm::onAction,
    )
}

@Composable
fun Tcp(
    state: NetState = NetState(),
    pagingMessages: LazyPagingItems<Message>,
    onAction: (NetAction) -> Unit = {}
) {
    val isConnecting = state.result != NetResult.Close
    val info = when (state.result) {
        NetResult.Server -> "Server address: ${state.model.server.localIp}:${state.model.server.port}"
        NetResult.Client -> "Connect server: ${state.model.client.ip}:${state.model.client.port}"
        NetResult.Close -> "Close"
        NetResult.Error -> "Error: ${state.error}"
    }
    Scaffold(
        topBar = {
            TopBar(
                isConnecting = isConnecting,
                connectInfo = info,
                onAction = { onAction(NetAction.Top(it)) }
            )
        },
        bottomBar = {
            MessageBottomBar(
                enabled = state.result == NetResult.Client || state.result == NetResult.Server,
                expanded = state.expandedBottomBar,
                state = state.bottomBarSettings,
                message = state.message,
                onAction = { onAction(NetAction.Bottom(it)) },
            )
        }
    ) {
        ChatMessageLazyColumn(
            modifier = Modifier
                .padding(it)
                // .then(if (state.expandedBottomBar) Modifier.padding(bottom = 100.dp) else Modifier)
                .fillMaxSize(),
            pagingMessages
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTabs() {
    EpisodeTheme {
        Scaffold(
            topBar = {
                TopBar()
            },
            bottomBar = {
                MessageBottomBar()
            }
        ) {
            PreviewMessageLazyColumn(Modifier.padding(it))
        }
    }
}