package com.connor.episode.ui.common

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.connor.episode.core.utils.formatSmartly
import com.connor.episode.domain.model.business.Message
import com.connor.episode.ui.state.rememberIsScrollingUp
import com.connor.episode.ui.theme.EpisodeTheme

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
            modifier = Modifier.widthIn(max = 280.dp),
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
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = message.content,
                    style = TextStyle(
                        fontSize = 16.sp,
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
    messages: List<Message> = (0..5).map { Message(it.toString(), it % 2 == 0) },
) {
    val listState = rememberLazyListState()
    val isScrollingUp by listState.rememberIsScrollingUp()
    LaunchedEffect(messages.size) {
        listState.animateScrollToItem(
            index = (messages.size - 1).coerceAtLeast(0),
            scrollOffset = 0
        )
    }
    LazyColumn(
        state = listState,
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
        items(messages) { msg ->
            ChatBubble(msg)
        }
        items(1) {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    EpisodeTheme {
        ChatMessageLazyColumn()
    }
}