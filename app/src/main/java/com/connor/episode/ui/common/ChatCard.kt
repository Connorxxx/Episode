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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.connor.episode.models.Message
import com.connor.episode.ui.theme.EpisodeTheme
import com.connor.episode.utils.formatSmartly

@Composable
fun ChatBubble(
    message: Message,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = if (message.isMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Card(
            modifier = Modifier
                .padding(8.dp)
                .widthIn(max = 280.dp),
            shape = RoundedCornerShape(
                topStart = if (message.isMe) 16.dp else 0.dp,
                topEnd = if (message.isMe) 0.dp else 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isMe)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onPrimary
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
                    text = message.time.formatSmartly(),
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = if (message.isMe) MaterialTheme.colorScheme.outlineVariant else Color.Gray
                    ),
                    modifier = Modifier.align(Alignment.End)
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
    LaunchedEffect(messages.size) {
        listState.scrollToItem(messages.size)
    }
    LazyColumn(
        state = listState,
        //contentPadding = it,
        modifier = modifier
            .fillMaxSize()
            //.padding(bottom = it.calculateBottomPadding())
            .background(MaterialTheme.colorScheme.surfaceVariant),
        verticalArrangement = Arrangement.spacedBy(4.dp),
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