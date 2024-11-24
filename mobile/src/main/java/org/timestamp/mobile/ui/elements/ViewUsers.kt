package org.timestamp.mobile.ui.elements

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseUser
import io.ktor.util.sha1
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.timestamp.lib.dto.EventDTO
import org.timestamp.mobile.ui.theme.ubuntuFontFamily
import org.timestamp.mobile.R
import org.timestamp.mobile.models.AppViewModel
import org.timestamp.mobile.ui.theme.Colors
import kotlin.math.roundToInt

@Composable
fun ViewUsers(
    event: EventDTO,
    onDismissRequest: () -> Unit,
    properties: DialogProperties = DialogProperties(),
    currentUser: FirebaseUser,
    viewModel: AppViewModel
) {
    var linkCopiedDialog = remember { mutableStateOf(false) }

    val clipBoardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val users = event.users.sortedBy { it.name }
    if (linkCopiedDialog.value) {
        AlertDialog(
            onDismissRequest = {
                linkCopiedDialog.value = false
            },
            title = {
                Text(
                    text = "Success",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Link successfully copied to clipboard!")
                   },
            confirmButton = {
                TextButton(
                    onClick = {
                        linkCopiedDialog.value = false
                    },
                ) {
                    Text(
                        text = "OK",
                        fontFamily = ubuntuFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },

        )
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties.let {
            DialogProperties(
                usePlatformDefaultWidth = false
            )
        }
    ) {
        Card(
            modifier = Modifier
                .shadow(6.dp, shape = RoundedCornerShape(32.dp))
                .background(color = Color.White, shape = RoundedCornerShape(32.dp))
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
        ) {
            Column(
                modifier = Modifier
                    .background(color = Color.White)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Event Attendees",
                    fontFamily = ubuntuFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    modifier = Modifier
                        .padding(16.dp)
                )
                Divider(
                    modifier = Modifier
                        .fillMaxWidth(0.9f),
                    thickness = 2.dp,
                    color = Color.LightGray
                )
                Row {
                    Text(
                        text = "Invite Link",
                        fontFamily = ubuntuFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier
                            .padding(12.dp)
                    )
                    IconButton(
                        onClick = {
                            CoroutineScope(Dispatchers.Main).launch {
                                val link = viewModel.getEventLink(event.id!!) ?: "Error"
//                                clipBoardManager.setText(AnnotatedString(link))
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, link)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, null)
                                context.startActivity(shareIntent)
//                                linkCopiedDialog.value = true
                            }
                        },
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .size(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.link_icon),
                            contentDescription = "link icon",
                            tint = Colors.Bittersweet,
                            modifier = Modifier
                                .size(24.dp)
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth(0.9f)
                        .heightIn(max = 500.dp)
                ) {
                    repeat(30) { // for testing purposes, remove later
                    for (user in users) {
                        val isOwner = event.creator == user.id
                        var est = 0
                        var distance : Double = 0.0
                        if (user.timeEst != null) {
                            est = (user.timeEst!! / 1000 / 60).toInt()
                        }
                        var unitKm = false
                        if (user.distance != null) {
                            distance = user.distance!!
                            if (distance >= 1000) {
                                distance /= 1000
                                unitKm = true
                            }
                        }
                        val hasArrived = event.arrivals.any { arrival -> arrival.userId == user.id }
                        item {
                            Row {
                                Image(
                                    painter = rememberAsyncImagePainter(user.pfp),
                                    contentDescription = "user pfp",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .padding(horizontal = 3.dp)
                                        .size(24.dp)
                                        .clip(CircleShape)
                                )
                                Column {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.White)
                                            .padding(4.dp)
                                    ) {
                                        val userName = user.name
                                        var suffix = ""
                                        if (isOwner) suffix = "$suffix (Owner)"
                                        if (currentUser.uid == user.id) {
                                            suffix = "$suffix (Me)"
                                        }
                                        Text(
                                            text = userName,
                                            fontFamily = ubuntuFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier
                                                .width(150.dp)
                                                .padding(vertical = 3.dp)
                                        )
                                        Text(
                                            text = suffix,
                                            fontFamily = ubuntuFontFamily,
                                            fontSize = 14.sp,
                                            modifier = Modifier
                                        )
                                    }
                                    Text(
                                        text = user.email,
                                        fontFamily = ubuntuFontFamily,
                                        fontSize = 14.sp,
                                    )
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                if (!hasArrived) {
                                    Column {
                                        Row {
                                            Icon(
                                                painter = painterResource(id = R.drawable.clock_icon),
                                                tint = Color.Unspecified,
                                                contentDescription = "user ETA",
                                                modifier = Modifier
                                                    .size(18.dp)
                                            )
                                            Text(
                                                text = "${est}min",
                                                fontFamily = ubuntuFontFamily,
                                                fontSize = 14.sp,
                                                modifier = Modifier
                                                    .padding(3.dp)
                                            )
                                        }
                                        Row {
                                            Icon(
                                                painter = painterResource(id = R.drawable.location_icon),
                                                tint = Color.Unspecified,
                                                contentDescription = "user distance",
                                                modifier = Modifier
                                                    .size(18.dp)
                                            )

                                            var distanceString : String
                                            if (unitKm) {
                                                distanceString = String.format("%.1f", distance)
                                                distanceString = "${distanceString}km"
                                            } else {
                                                distanceString = distance.roundToInt().toString()
                                                distanceString = "${distanceString}m"
                                            }
                                            Text(
                                                text = distanceString,
                                                fontFamily = ubuntuFontFamily,
                                                fontSize = 14.sp,
                                                color = Colors.Black,
                                                modifier = Modifier
                                                    .padding(3.dp)
                                            )
                                        }
                                    }
                                } else {
                                    Icon(
                                        painter = painterResource(id = R.drawable.arrived_icon),
                                        contentDescription = "arrived icon",
                                        tint = Color.Green,
                                        modifier = Modifier
                                            .size(24.dp)
                                    )
                                }
                            }
                            Divider(
                                color = Color.LightGray,
                                thickness = 2.dp
                            )
                        }
                    }
                    }

                }
                TextButton(
                    onClick = onDismissRequest,
                    colors = ButtonColors(
                        containerColor = Color(0xFFFF6F61),
                        contentColor = Color(0xFFFFFFFF),
                        disabledContainerColor = Color(0xFFFF6F61),
                        disabledContentColor = Color(0xFFFFFFFF)
                    ),
                    modifier = Modifier
                        .align(alignment = Alignment.CenterHorizontally)
                        .padding(16.dp),
                    shape = RoundedCornerShape(size = 16.dp),
                ) {
                    Text(
                        text = "Done",
                        fontSize = 20.sp,
                        fontFamily = ubuntuFontFamily,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}