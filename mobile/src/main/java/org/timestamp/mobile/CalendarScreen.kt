package org.timestamp.mobile

import androidx.compose.foundation.background
import org.timestamp.mobile.eventList
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Typography
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.text.TextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.boguszpawlowski.composecalendar.SelectableCalendar
import org.timestamp.mobile.ui.theme.Colors
import org.timestamp.mobile.ui.elements.DynamicCalendar
import org.timestamp.mobile.ui.elements.EventData
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month

val ubuntuFontFamily = FontFamily(
    Font(R.font.ubuntu_regular),  // Regular
    Font(R.font.ubuntu_bold, FontWeight.Bold)  // Bold
)

val calendarTypography = Typography(
    body1 = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp
    ),
    h1 = TextStyle(
        color = Colors.Black,
        fontFamily = ubuntuFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
    ),
    h4 = TextStyle(
        color = Colors.Bittersweet,
        fontFamily = ubuntuFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
    ),
    h5 = TextStyle(
        color = Colors.Black,
        fontFamily = ubuntuFontFamily,
        fontSize = 24.sp,
    ),
    h6 = TextStyle(
        color = Colors.Black,
        fontFamily = ubuntuFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
    )
)

@Composable
fun CalendarScreen() {

    var selectedDate by remember { mutableStateOf<LocalDate?>(null)}

    val eventsOnSelectedDate = remember(selectedDate) {
        eventList.filter { it.date.toLocalDate() == selectedDate }
    }
    MaterialTheme(typography = calendarTypography) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Spacer(modifier = Modifier.size(70.dp))
                Text(text = "Calendar",
                    style = MaterialTheme.typography.h1,
                    modifier = Modifier
                        .offset(x = 20.dp)
                )
                Spacer(modifier = Modifier.size(15.dp))
                DynamicCalendar { date ->
                    selectedDate = date
                }

                if (selectedDate != null) {
                    Text(
                        text = "Events on ${selectedDate.toString()}",
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.padding(start = 16.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.padding(top = 8.dp, start = 16.dp)
                    ) {
                        if (eventsOnSelectedDate.isNotEmpty()) {
                            items(eventsOnSelectedDate) { event ->
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(
                                        defaultElevation = 4.dp
                                    ),
                                    modifier = Modifier
                                        .padding(vertical = 4.dp)
                                        .width(375.dp)
                                ) {
                                    Row(modifier = Modifier
                                        .fillMaxWidth()
                                        .height(IntrinsicSize.Min)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .width(10.dp)
                                                .background(Colors.Bittersweet)
                                        )
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Colors.White)
                                                .padding(16.dp)
                                        ) {
                                            Text(
                                                text = event.name
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            item {
                                Text("No events scheduled yet!")
                            }
                        }
                    }
                }
            }
        }
    }
}