package com.connor.episode.features.websocket

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.connor.episode.features.tcp.Tcp
import com.connor.episode.features.tcp.components.TCPSettingDialog
import com.connor.episode.features.common.ui.theme.EpisodeTheme
import com.connor.episode.features.tcp.PreviewTabs

@Composable
fun WebSocketScreen(vm: WebSocketViewModel = hiltViewModel()) {
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

@Preview(showBackground = true)
@Composable
private fun Preview() {
    PreviewTabs()
}