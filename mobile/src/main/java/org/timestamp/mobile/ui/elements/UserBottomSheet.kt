package org.timestamp.mobile.ui.elements

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.timestamp.lib.dto.EventDTO
import org.timestamp.lib.dto.EventUserDTO
import org.timestamp.lib.dto.timeString
import org.timestamp.mobile.getActivity
import org.timestamp.mobile.ui.theme.Colors
import org.timestamp.mobile.viewmodels.EventViewModel
import java.time.OffsetDateTime

val testUsers = listOf(
    EventUserDTO(
        name = "John Doe",
        email = "asdasds@gmail.com",
        arrived = false,
        timeEst = 1000L,
        pfp = "https://lh3.googleusercontent.com/a/ACg8ocLJEXurWDwRTHAoHuxuAABsPT3Wne9QVSzQUxugBjkt_55RIU0=s96-c",
    ),
    EventUserDTO(
        name = "Jane As",
        email = "jameas@hotmail.com",
        arrived = false,
        timeEst = 20000L,
        pfp = "https://lh3.googleusercontent.com/a/ACg8ocKa52qVuuqk3tKfiEqfI5Sbk12VSyKpc8XGAB5rNoOGGPBeaQ=s96-c"
    ),
    EventUserDTO(
        name = "John Smith",
        email = "smith@outlook.com",
        arrived = false,
        timeEst = 30000L,
        pfp = "https://lh3.googleusercontent.com/a/ACg8ocKa52qVuuqk3tKfiEqfI5Sbk12VSyKpc8XGAB5rNoOGGPBeaQ=s96-c"
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserBottomSheet(
    event: EventDTO,
    arrival: OffsetDateTime,
    isOwner: Boolean,
    onDismiss: () -> Unit
) {
    val users = event.users
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val activity = LocalContext.current.getActivity()
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        containerColor = Colors.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight(0.6f),
        ) {
            if (isOwner) {
                Button(
                    onClick = { scope.shareLink(event, activity) },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Colors.Bittersweet)
                ) {
                    Text("Share", color = Colors.White)
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Colors.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                HorizontalDivider(
                    thickness = 1.dp,
                    color = Colors.Platinum,
                    modifier = Modifier.padding(top = 8.dp).padding(horizontal = 16.dp)
                )
            }

            users.forEach {
                val timeEst = it.timeEst
                val arrivalTime = OffsetDateTime.now().plusSeconds((timeEst ?: 0L).floorDiv(1000))
                val isOnTime = arrivalTime.isBefore(arrival)

                ListItem(
                    overlineContent = { Text(it.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    headlineContent = { Text(it.email, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    trailingContent = {
                        if (!it.arrived && timeEst != null) {
                            Text("ETA: ${it.timeString()}")
                        }
                    },
                    supportingContent = {
                        when {
                            it.arrived -> Text("Arrived", color = Colors.Green)
                            isOnTime -> Text("On time", color = Colors.Green)
                            else -> Text("Late", color = Colors.BittersweetDark)
                        }
                    },
                    leadingContent = {
                        val painter = rememberAsyncImagePainter(it.pfp)
                        Image(
                            painter = painter,
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Colors.White)
                )
            }

        }
    }
}

fun CoroutineScope.shareLink(
    event: EventDTO,
    activity: ComponentActivity
) = this.launch {
    val eventVm: EventViewModel by activity.viewModels()
    val link = eventVm.getEventLink(event.id!!) ?: "An error occurred"
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, link)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, null)
    activity.startActivity(shareIntent)
}

