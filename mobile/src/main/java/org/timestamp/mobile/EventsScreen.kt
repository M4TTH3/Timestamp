package org.timestamp.mobile

import android.content.Context
import android.util.Log
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.client.request.headers
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.timestamp.backend.viewModels.EventDetailed
import org.timestamp.mobile.ui.elements.CreateEvent
import org.timestamp.mobile.ui.elements.EventBox
import org.timestamp.mobile.ui.theme.ubuntuFontFamily

val eventList: MutableList<EventDetailed> = mutableStateListOf()

@Composable
fun EventsScreen(auth: FirebaseAuth) {
    val context = LocalContext.current
    val createEvents = remember { mutableStateOf(false) }
    val hasEvents = remember { mutableStateOf(false) }
    if (createEvents.value) {
        CreateEvent(
            onDismissRequest = { createEvents.value = false },
            onConfirmation = {
                pushBackendEvents(context, auth)
                createEvents.value = false
            }
        )
    }
    if (eventList.isNotEmpty()) {
        hasEvents.value = true
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
            if (!hasEvents.value) {
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
                    eventList.sortBy { it.arrival }
                    for (event in eventList) {
                        item {
                            EventBox(event, auth)
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

fun pushBackendEvents(context: Context, auth: FirebaseAuth) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val endpoint = "${context.getString(R.string.backend_url)}/events"
            val events = eventList.map { it }
            val tokenResult = auth.currentUser?.getIdToken(false)?.await()

            val res = ktorClient.post(endpoint) {
                contentType(ContentType.Application.Json)
                setBody(events.last())
                headers {
                    append("Authorization", "Bearer ${tokenResult?.token}")
                }
            }

            // Check response
            withContext(Dispatchers.Main) {
                if (res.status == HttpStatusCode.OK) {
                    Log.d("Events successfully pushed to backend", res.bodyAsText())
                } else {
                    Log.println(Log.ERROR, "Backend Events Push Error", "res status: ${res.status}, body: ${events.last()}")
                }
            }
        } catch (e: Exception) {
            Log.e("Backend Events Push Error", e.toString())
        }
    }
}
