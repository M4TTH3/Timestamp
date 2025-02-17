package org.timestamp.mobile.ui.elements

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.size
import androidx.compose.material.IconButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.timestamp.mobile.ui.theme.Colors

@Composable
fun DropdownIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    iconSize: Dp,
    dropdownContent: @Composable (ColumnScope.() -> Unit)
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
    ) {
        IconButton(onClick = { expanded = !expanded }, modifier = Modifier.size(iconSize)) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(iconSize))
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            content = dropdownContent,
            containerColor = Colors.White,
            shadowElevation = 8.dp
        )
    }

}