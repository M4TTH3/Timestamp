package org.timestamp.mobile

import android.widget.ImageButton
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import org.timestamp.mobile.ui.elements.CreateEvent

@Composable
fun EventsScreen(
    hasEvents: Boolean
) {
    val ubuntuFontFamily = FontFamily(
        Font(R.font.ubuntu_regular),  // Regular
        Font(R.font.ubuntu_bold, FontWeight.Bold)  // Bold
    )

    val createEvents = remember { mutableStateOf(false) }
    if (createEvents.value) {
        CreateEvent(
            onDismissRequest = { createEvents.value = false },
            onConfirmation = { createEvents.value = false }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        IconButton(onClick = {
            //todo
        },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .offset(y=24.dp)
                .size(48.dp)
            ) {
            Icon(painter = painterResource(id = R.drawable.notification_bell),
                contentDescription = "Notification Bell",
                modifier = Modifier.size(32.dp),
                tint = Color.Unspecified)
        }
        Text(text = "Upcoming Events...",
            color = Color(0xFF000000),
            fontFamily = ubuntuFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            modifier = Modifier
                .offset(x = 20.dp, y = 70.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!hasEvents) {
                Text(text = "Add an\nEvent!",
                    color = Color(0xFFE5E6EA),
                    fontFamily = ubuntuFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 80.sp,
                    textAlign = TextAlign.Start,
                    lineHeight = 80.sp
                )
            }
        }
        IconButton(onClick = {
            createEvents.value = true
        },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .offset(y=(-50).dp)
                .size(54.dp)
        ) {
            Icon(painter = painterResource(id = R.drawable.plus_button),
                contentDescription = "Add Event Button",
                modifier = Modifier.size(54.dp),
                tint = Color.Unspecified)
        }
    }
}


