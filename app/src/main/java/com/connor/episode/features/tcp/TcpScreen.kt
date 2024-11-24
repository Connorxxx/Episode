package com.connor.episode.features.tcp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.connor.episode.core.utils.logCat
import com.connor.episode.ui.common.PressIconButton
import com.connor.episode.ui.theme.EpisodeTheme

@Composable
fun TcpScreen() {
    Tcp()
}

@Composable
private fun Tcp() {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = {
            "on button click".logCat()
        }) {
            Text(text = "Tcp")
        }
        PressIconButton(
            onClick = {
                "onClick".logCat()
            },
            onLongClick = {
                "onLongClick".logCat()
            },
            onLongClickRelease = {
                "onLongClickRelease".logCat()
            }
        ) {
            Icon(Icons.Default.ChevronRight, "Increase")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    EpisodeTheme {
        Tcp()
    }
}