package org.timestamp.mobile.ui.elements

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsBike
import androidx.compose.material.icons.automirrored.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.Commute
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import org.timestamp.shared.dto.TravelMode
import org.timestamp.mobile.ui.theme.Colors
import org.timestamp.mobile.ui.theme.tsTypography

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun TravelModeModal(
    onDismiss: () -> Unit = {},
    onConfirm: (TravelMode?) -> Unit = {}
) = BasicAlertDialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(
        dismissOnBackPress = true,
        dismissOnClickOutside = true,
        usePlatformDefaultWidth = false
    ),
    modifier = Modifier
        .padding(horizontal = 12.dp)
) {
    Column(
        modifier = Modifier
            .clip(shape = ShapeDefaults.Medium)
            .background(color = Colors.White)
            .fillMaxWidth()
            .height(250.dp)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val size = LocalConfiguration.current.screenWidthDp.dp

        Text("Select a Transportation!", color = Colors.Bittersweet, style = tsTypography.headlineMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Specify for a smart alarm", color = Colors.Gray, style = tsTypography.bodySmall)
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf(
                TravelMode.Car to Icons.Outlined.DirectionsCar,
                TravelMode.Bike to Icons.AutoMirrored.Outlined.DirectionsBike,
                TravelMode.Foot to Icons.AutoMirrored.Outlined.DirectionsWalk
            ).forEach { (mode, icon) ->
                Button(
                    shape = ShapeDefaults.Medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Colors.TeaRose,
                        contentColor = Colors.Black
                    ),
                    elevation = ButtonDefaults.elevatedButtonElevation(
                        defaultElevation = 4.dp
                    ),
                    modifier = Modifier
                        .width(size * 0.27f)
                        .heightIn(min = 100.dp, max = 150.dp),
                    onClick = { onConfirm(mode) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, modifier = Modifier.size(60.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            shape = ShapeDefaults.Medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Colors.Black
            ),
            border = BorderStroke(2.dp, Colors.Platinum),
            onClick = { onConfirm(null) }
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Auto", style = tsTypography.headlineSmall, color = Colors.BlackFaint)
                Spacer(modifier = Modifier.width(10.dp))
                Icon(Icons.Outlined.Commute, contentDescription = null, tint = Colors.BlackFaint)
            }
        }
    }
}