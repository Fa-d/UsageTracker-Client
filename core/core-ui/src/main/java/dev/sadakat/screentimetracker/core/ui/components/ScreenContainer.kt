package dev.sadakat.screentimetracker.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.sadakat.screentimetracker.core.ui.theme.CoreSpacing
import dev.sadakat.screentimetracker.core.ui.theme.CoreTextStyles

@Composable
fun ScreenContainer(
    title: String,
    modifier: Modifier = Modifier,
    content: LazyListScope.() -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(CoreSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(CoreSpacing.sectionSpacing)
    ) {
        item {
            Text(
                text = title,
                style = CoreTextStyles.screenTitle,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        content()
    }
}