package org.timestamp.mobile.ui.elements

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
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import io.ktor.util.sha1
import org.timestamp.backend.viewModels.EventDetailed
import org.timestamp.mobile.ui.theme.ubuntuFontFamily
import org.timestamp.mobile.R
import org.timestamp.mobile.ui.theme.Colors

@Composable
fun ViewUsers(
    event: EventDetailed,
    onDismissRequest: () -> Unit,
    properties: DialogProperties = DialogProperties(),
) {
    val users = event.users.sortedBy { it.name }
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
                        onClick = { /*TODO*/ },
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
                    repeat(5) { // for testing purposes, remove later
                    for (user in users) {
                        val isOwner = event.creator == user.name
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White)
                                    .padding(4.dp)
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(user.pfp),
                                    contentDescription = "user pfp",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, Color.Gray, CircleShape)
                                )
                                var userName = user.name
                                if (isOwner) userName = "$userName (Owner)"
                                Text(
                                    text = userName,
                                    fontFamily = ubuntuFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    modifier = Modifier
                                        .padding(3.dp)
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = "On Time",
                                    fontFamily = ubuntuFontFamily,
                                    fontSize = 14.sp,
                                    color = Color.Green,
                                    modifier = Modifier
                                        .padding(3.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = { /*TODO*/ },
                                    modifier = Modifier
                                        .size(20.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.remove),
                                        contentDescription = "remove user icon",
                                        modifier = Modifier
                                            .size(20.dp),
                                        tint = Color.Unspecified
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