package com.connor.episode.features.ble

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.connor.episode.domain.model.business.Message
import com.connor.episode.domain.model.uimodel.BleAction
import com.connor.episode.domain.model.uimodel.BleState
import com.connor.episode.domain.model.uimodel.ConnectState
import com.connor.episode.domain.model.uimodel.ServerState
import com.connor.episode.features.ble.components.BleSettingDialog
import com.connor.episode.features.common.ui.common.ChatMessageLazyColumn
import com.connor.episode.features.common.ui.common.TopBar

@Composable
fun BleScreen(vm: BleVIewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val pagingMessages = vm.messagePagingFlow.collectAsLazyPagingItems()
    Ble(state = state, pagingMessages = pagingMessages, onAction = vm::onAction)
    if (state.isShowSettingDialog) BleSettingDialog(state, vm::onAction)
}

@Composable
fun Ble(
    state: BleState = BleState(),
    pagingMessages: LazyPagingItems<Message>,
    onAction: (BleAction) -> Unit = {}
) {
    val isConnecting = state.serverState != ServerState.Inactive || state.connectState != ConnectState.Disconnected
    Scaffold(
        topBar = {
            TopBar(
                isConnecting = isConnecting,
                connectInfo = state.info,
                onAction = { onAction(BleAction.Top(it)) }
            )
        },
        bottomBar = {
            //MessageBottomBar()
        }
    ) {
        ChatMessageLazyColumn(
            modifier = Modifier
                .padding(it).fillMaxSize(),
            pagingMessages
        )
    }
}