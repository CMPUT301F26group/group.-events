package com.example.lottcamp.ui.screens.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.lottcamp.controller.ChatBubble
import com.example.lottcamp.ui.components.AppMessageBubble
import com.example.lottcamp.ui.components.AppMessageInput
import com.example.lottcamp.ui.components.AppMutedText
import com.example.lottcamp.ui.components.AppTopBar
import com.example.lottcamp.ui.theme.LottCampTheme

@Composable
fun ChatScreen(
    title: String,
    subtitle: String,
    bubbles: List<ChatBubble>,
    draft: String,
    onDraftChange: (String) -> Unit,
    sending: Boolean,
    onSend: () -> Unit,
    error: String?,
    onDismissError: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    error?.let {
        AlertDialog(
            onDismissRequest = onDismissError,
            confirmButton = { TextButton(onClick = onDismissError) { Text("OK") } },
            title = { Text("Something went wrong") },
            text = { Text(it) },
        )
    }

    val listState = rememberLazyListState()
    LaunchedEffect(bubbles.size) {
        if (bubbles.isNotEmpty()) listState.animateScrollToItem(bubbles.lastIndex)
    }

    Column(modifier = modifier.fillMaxSize()) {
        AppTopBar(title = title, onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp, top = 16.dp, end = 16.dp)
        ) {
            AppMutedText(subtitle)
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(bubbles, key = { it.id }) { bubble ->
                    AppMessageBubble(text = bubble.text, time = bubble.time, mine = bubble.mine)
                }
            }
        }

        AppMessageInput(
            value = draft,
            onValueChange = onDraftChange,
            onSend = onSend,
            enabled = !sending,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    LottCampTheme {
        ChatScreen(
            title = "Alex Morgan",
            subtitle = "Pine Ridge Camp · RES-2038",
            bubbles = listOf(
                ChatBubble("1", "Hi Alex, check-in starts at 3 PM.", "Today · 11:02 AM", true),
                ChatBubble("2", "Thanks! We'll arrive around 4.", "Today · 11:10 AM", false),
            ),
            draft = "",
            onDraftChange = {},
            sending = false,
            onSend = {},
            error = null,
            onDismissError = {},
            onBack = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
        )
    }
}