package org.timestamp.mobile

import android.widget.ImageButton
import androidx.annotation.FloatRange
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.input.pointer.motionEventSpy
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
import org.timestamp.mobile.ui.elements.Event
import org.timestamp.mobile.ui.elements.EventData
import org.timestamp.mobile.ui.theme.ubuntuFontFamily
import java.time.LocalDateTime

val eventList: MutableList<EventData> = mutableStateListOf()

@Composable
fun EventsScreen(
    hasEvents: Boolean
) {
    val createEvents = remember { mutableStateOf(false) }
    if (createEvents.value) {
        CreateEvent(
            onDismissRequest = { createEvents.value = false },
            onConfirmation = {
                createEvents.value = false
            }
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
        Column(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            Text(
                text = "Upcoming Events...",
                color = Color(0xFF000000),
                fontFamily = ubuntuFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                modifier = Modifier
                    .offset(x = 20.dp, y = 70.dp)
            )
            if (!hasEvents) {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Add an\nEvent!",
                        color = Color(0xFFE5E6EA),
                        fontFamily = ubuntuFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 80.sp,
                        textAlign = TextAlign.Start,
                        lineHeight = 80.sp
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(92.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .animateContentSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    eventList.sortBy { it.date }
                    for (event in eventList) {
                        item {
                            Event(event)
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(120.dp))
                    }
                }
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
