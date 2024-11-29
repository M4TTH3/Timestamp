package org.timestamp.mobile.ui.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.android.gms.maps.model.LatLng
import org.timestamp.lib.dto.EventDTO
import org.timestamp.mobile.models.EventViewModel
import org.timestamp.mobile.ui.theme.ubuntuFontFamily
import java.time.format.DateTimeFormatter

@Composable
fun AcceptEvent(
    eventViewModel: EventViewModel,
    event: EventDTO
) {

    val dateTimeFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy h:mma")
    val context = LocalContext.current
    Dialog(
        onDismissRequest = {  }
    ) {
        Card (
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .height(400.dp)
                .shadow(6.dp, shape = RoundedCornerShape(32.dp))
                .background(Color.White, shape = RoundedCornerShape(32.dp)),
            shape = RoundedCornerShape(32.dp)
        ) {
            Text(
                modifier = Modifier
                    .padding(16.dp),
                text = "Join Event",
                color = Color.Black,
                fontFamily = ubuntuFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
            Divider(
                color = Color.LightGray,
                thickness = 2.dp,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(bottom = 16.dp)
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .shadow(
                        4.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = Color(0x33000000)
                    )
                    .border(1.dp, Color.LightGray, shape = RoundedCornerShape(16.dp))
                    .background(Color.White, shape = RoundedCornerShape(16.dp)),
                textAlign = TextAlign.Center,
                text = event.name
            )
            Text(
                text = event.arrival.toLocalDateTime().format(dateTimeFormatter)
            )
            EventMap(
                event.address,
                event.name,
                LatLng(event.latitude, event.longitude),
                context
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = eventViewModel::cancelPendingEvents) {
                    Text("Cancel")
                }

                Button(onClick = eventViewModel::joinPendingEvent) {
                    Text("Accept")
                }
            }
        }
    }
}