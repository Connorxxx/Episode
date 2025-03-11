package com.connor.episode.features.common.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.connor.episode.core.utils.formatSmartly
import com.connor.episode.domain.model.business.Message
import com.connor.episode.features.common.ui.state.rememberIsScrollingUp
import com.connor.episode.features.common.ui.state.rememberKeyboardAnimationCompleteState
import com.connor.episode.features.common.ui.theme.EpisodeTheme
import com.connor.episode.features.common.ui.theme.messageName
import com.connor.episode.features.common.ui.theme.messageNameOther

@Composable
fun AnimatedMessageItem(
    message: Message,
    isNewMessage: Boolean,
) {
    // 使用AnimatedVisibility为新消息添加动画
    AnimatedVisibility(
        visible = true, // 始终可见
        enter = if (isNewMessage) {
            // 仅为新消息应用入场动画
            slideInVertically(
                // 从底部滑入
                initialOffsetY = { height -> height },
                // 使用弹簧动画，营造弹出效果
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeIn(
                // 淡入效果
                initialAlpha = 0f,
                animationSpec = tween(durationMillis = 300)
            )
        } else {
            // 非新消息无入场动画
            EnterTransition.None
        }
    ) {
        // 消息气泡的内容
        ChatBubble(message)
    }
}

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
    messages: List<Message> = (0..5).map { Message(it, "Connor","Hello Im message No.$it", it % 2 == 0) },
) {

    val listState = rememberLazyListState()
    val isScrollingUp by listState.rememberIsScrollingUp()
    LaunchedEffect(messages.size) {
        val lastIndex = (messages.size - 1).coerceAtLeast(0)
//        if (lastIndex > 0) {
//            listState.scrollToItem(lastIndex - 1)
//            delay(50)
//        }
        listState.animateScrollToItem(lastIndex)
    }
    val imeVisible by rememberKeyboardAnimationCompleteState()
    LaunchedEffect(imeVisible) {
        if (imeVisible) listState.animateScrollToItem((messages.size - 1).coerceAtLeast(0))
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
        items(messages, key = { it.id }) { msg ->
            ChatBubble(msg, Modifier.animateItem())
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