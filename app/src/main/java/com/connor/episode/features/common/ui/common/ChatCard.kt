package com.connor.episode.features.common.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.connor.episode.core.utils.formatSmartly
import com.connor.episode.domain.model.business.Message
import com.connor.episode.features.common.ui.state.rememberKeyboardAnimationCompleteState
import com.connor.episode.features.common.ui.theme.EpisodeTheme
import com.connor.episode.features.common.ui.theme.messageName
import com.connor.episode.features.common.ui.theme.messageNameOther

@Composable
fun ChatBubble(
    message: Message,
    modifier: Modifier = Modifier
) {
    val text = with(message) {
        if (isMe) "${time.formatSmartly()}  (${type})" else "(${type})  ${time.formatSmartly()}"
    }
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = if (message.isMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Card(
            modifier = Modifier.widthIn(max = LocalConfiguration.current.screenWidthDp.dp * 0.9f),
            shape = RoundedCornerShape(
                topStart = if (message.isMe) 20.dp else 4.dp,
                topEnd = if (message.isMe) 4.dp else 20.dp,
                bottomStart = 20.dp,
                bottomEnd = 20.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isMe) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onPrimary
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = .4.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = message.name,
                    modifier = Modifier
                        .align(if (message.isMe) Alignment.End else Alignment.Start),
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (message.isMe) messageName else messageNameOther
                    ),
                    softWrap = true,
                    overflow = TextOverflow.Visible,
                    textAlign = TextAlign.Justify
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.content,
                    style = TextStyle(
                        fontSize = 18.sp,
                        color = if (message.isMe) Color.White else Color.Black
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = text,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = if (message.isMe) MaterialTheme.colorScheme.outlineVariant else Color.Gray
                    ),
                    modifier = Modifier.align(if (message.isMe) Alignment.End else Alignment.Start),
                )
            }
        }
    }
}

@Composable
fun ChatMessageLazyColumn(
    modifier: Modifier = Modifier,
    pagingMessages: LazyPagingItems<Message>
) {
    val listState = rememberLazyListState()
    LaunchedEffect(pagingMessages.itemCount) {
        // val lastIndex = (messages.size - 1).coerceAtLeast(0)
        // listState.animateScrollToItem(lastIndex)
        val isNearTop = listState.firstVisibleItemIndex <= 1
        if (pagingMessages.itemCount > 0 && isNearTop) listState.animateScrollToItem(0)
    }
    val imeVisible by rememberKeyboardAnimationCompleteState()
    LaunchedEffect(imeVisible) {
        if (imeVisible && pagingMessages.itemCount > 0) listState.animateScrollToItem(0)
    }
    BaseMessageLazyColumn(
        modifier = modifier,
        listState = listState
    ) {
        items(
            count = pagingMessages.itemCount,
            key = { pagingMessages[it]?.id ?: it }
        ) {
            pagingMessages[it]?.let { msg ->
                ChatBubble(msg, Modifier.animateItem())
            }
        }
        renderLoadPaging(pagingMessages)
    }
}

@Composable
fun BaseMessageLazyColumn(
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    dataItems: LazyListScope.() -> Unit
) {
    LazyColumn(
        state = listState,
        reverseLayout = true,
        //contentPadding = it,
        modifier = modifier
            .fillMaxSize()
            //.padding(bottom = it.calculateBottomPadding())
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(1) {
            Spacer(modifier = Modifier.height(48.dp))
        }
        dataItems()
        items(1) {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}


private fun LazyListScope.renderLoadPaging(pagingMessages: LazyPagingItems<Message>) {
    when (pagingMessages.loadState.refresh) {
        is LoadState.Loading -> {
            item { LoadingItem() }
        }

        is LoadState.Error -> {
            val error = (pagingMessages.loadState.refresh as LoadState.Error).error
            item {
                ErrorItem(
                    message = error.localizedMessage ?: "Unknown error",
                    onRetry = { pagingMessages.retry() }
                )
            }
        }

        is LoadState.NotLoading -> Unit
    }

    when (pagingMessages.loadState.prepend) {
        is LoadState.Loading -> {
            item { LoadingItem() }
        }

        is LoadState.Error -> {
            val error = (pagingMessages.loadState.prepend as LoadState.Error).error
            item {
                ErrorItem(
                    message = error.localizedMessage ?: "Unknown error",
                    onRetry = { pagingMessages.retry() }
                )
            }
        }

        is LoadState.NotLoading -> Unit
    }
}

@Composable
fun LoadingItem() {
    CircularProgressIndicator(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .wrapContentWidth(Alignment.CenterHorizontally)
    )
}

@Composable
fun ErrorItem(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error
        )
        Button(
            onClick = onRetry,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(text = "Retry")
        }
    }
}
@Composable
fun PreviewMessageLazyColumn(modifier: Modifier = Modifier) {
    BaseMessageLazyColumn(modifier) {
        val messages: List<Message> = (0..5).map {
            Message(
                it,
                "Connor",
                "Hello Im message No.$it",
                it % 2 == 0
            )
        }
        items(messages, key = { it.id }) { msg ->
            ChatBubble(msg, Modifier.animateItem())
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    EpisodeTheme {
        PreviewMessageLazyColumn()
    }
}