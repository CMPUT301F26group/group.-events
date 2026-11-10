package com.example.lottcamp.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lottcamp.ui.components.AppButton
import com.example.lottcamp.ui.components.AppButtonVariant
import com.example.lottcamp.ui.components.AppChipRow
import com.example.lottcamp.ui.components.AppDialog
import com.example.lottcamp.ui.components.AppInfoRow
import com.example.lottcamp.ui.components.AppMutedText
import com.example.lottcamp.ui.components.AppSearchBar
import com.example.lottcamp.ui.components.AppSectionTitle
import com.example.lottcamp.ui.components.AppTopBar

/**
 * Admin page made of a top bar and a scrolling list, with an optional search bar,
 * filter chips and section header above the list.
 *
 * @param title Screen title.
 * @param onBack Invoked when the back arrow is tapped.
 * @param query Current search text.
 * @param onQueryChange Called on every search edit. Null hides the search bar.
 * @param placeholder Search bar placeholder.
 * @param filters Filter chip labels. Empty hides the chips.
 * @param selectedFilter Index of the selected chip in [filters].
 * @param onFilterSelect Called with the index of the tapped chip.
 * @param sectionTitle Heading above the list. Null hides the header.
 * @param countText Muted text shown at the end of the section header, e.g. "8 users".
 * @param content List items.
 */
@Composable
fun AdminListPage(
    title: String,
    onBack: () -> Unit,
    query: String = "",
    onQueryChange: ((String) -> Unit)? = null,
    placeholder: String = "",
    filters: List<String> = emptyList(),
    selectedFilter: Int = 0,
    onFilterSelect: (Int) -> Unit = {},
    sectionTitle: String? = null,
    countText: String = "",
    content: LazyListScope.() -> Unit,
) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        AppTopBar(title, onBack)
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (onQueryChange != null) {
                item { AppSearchBar(query, onQueryChange, placeholder = placeholder) }
            }
            if (filters.isNotEmpty()) {
                item { AppChipRow(filters, selectedFilter, onFilterSelect) }
            }
            if (sectionTitle != null) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppSectionTitle(sectionTitle, Modifier.weight(1f))
                        AppMutedText(countText)
                    }
                }
            }
            content()
        }
    }
}

/**
 * Labelled group of rows on an [AdminDetailPage].
 *
 * @property title Heading above the rows. Null shows the rows without one.
 * @property rows Label to value pairs.
 */
data class DetailSection(val title: String?, val rows: List<Pair<String, String>>)

/**
 * Admin details page: a header, label/value sections, and two buttons at the bottom.
 * The remove button asks for confirmation before calling [onRemove].
 *
 * @param title Screen title.
 * @param onBack Invoked when the back arrow is tapped.
 * @param sections Information shown under the header.
 * @param secondaryText Label of the outlined button.
 * @param onSecondary Invoked when the outlined button is tapped.
 * @param removeText Label of the red remove button.
 * @param confirmTitle Title of the confirmation dialog.
 * @param confirmMessage Message of the confirmation dialog.
 * @param onRemove Invoked once the removal is confirmed.
 * @param header Content at the top of the page, e.g. an avatar and name.
 */
@Composable
fun AdminDetailPage(
    title: String,
    onBack: () -> Unit,
    sections: List<DetailSection>,
    secondaryText: String,
    onSecondary: () -> Unit,
    removeText: String,
    confirmTitle: String,
    confirmMessage: String,
    onRemove: () -> Unit,
    header: @Composable () -> Unit,
) {
    var confirming by rememberSaveable { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        AppTopBar(title, onBack)
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            header()
            sections.forEach { section ->
                section.title?.let { AppSectionTitle(it, Modifier.padding(top = 12.dp)) }
                section.rows.forEach { (label, value) -> AppInfoRow(label, value) }
            }
        }
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AppButton(secondaryText, onSecondary, Modifier.fillMaxWidth(), AppButtonVariant.Secondary)
            AppButton(removeText, { confirming = true }, Modifier.fillMaxWidth(), AppButtonVariant.Danger)
        }
    }

    if (confirming) {
        AppDialog(
            title = confirmTitle,
            message = confirmMessage,
            dismissText = "Cancel",
            onDismiss = { confirming = false },
            confirmText = "Remove",
            onConfirm = {
                confirming = false
                onRemove()
            },
        )
    }
}

/**
 * Notification or message log entry.
 *
 * @param title Bold first line.
 * @param subtitle Muted line under the title.
 * @param body Main text. Shown in a bordered box when [boxed].
 * @param time Muted timestamp at the bottom.
 * @param status Muted text at the end of the title line, e.g. "Delivered".
 * @param boxed Whether to draw [body] inside a box, like a chat message.
 */
@Composable
fun LogEntry(
    title: String,
    subtitle: String,
    body: String,
    time: String,
    status: String = "",
    boxed: Boolean = false,
) {
    val shape = MaterialTheme.shapes.medium
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row {
            Text(title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            AppMutedText(status)
        }
        AppMutedText(subtitle)
        Text(
            text = body,
            modifier = if (boxed) {
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, shape)
                    .border(1.dp, MaterialTheme.colorScheme.outline, shape)
                    .padding(12.dp)
            } else {
                Modifier
            },
        )
        AppMutedText(time)
    }
}
