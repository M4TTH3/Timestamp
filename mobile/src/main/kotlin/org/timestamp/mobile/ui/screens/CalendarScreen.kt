package org.timestamp.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.Typography
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.timestamp.shared.dto.EventDTO
import org.timestamp.mobile.NavBarV2
import org.timestamp.mobile.R
import org.timestamp.mobile.ui.elements.DynamicCalendar
import org.timestamp.mobile.ui.elements.ModalViewEventSheet
import org.timestamp.mobile.ui.theme.Colors
import org.timestamp.mobile.utility.Screen
import org.timestamp.shared.viewmodel.EventViewModel
import java.time.LocalDate

val ubuntuFontFamily = FontFamily(
    Font(R.font.ubuntu_regular),  // Regular
    Font(R.font.ubuntu_bold, FontWeight.Bold)  // Bold
)

@Composable
fun CalendarScreen(
    navigateCreateEvent: (EventDTO?) -> Unit,
    navigate: (Screen) -> Unit
) {
    val viewModel: EventViewModel = viewModel()
    val eventListState = viewModel.events.collectAsState()
    val eventList: MutableList<EventDTO> = eventListState.value.toMutableList()
    var selectedDate by remember { mutableStateOf<LocalDate?>(null)}
    var refreshTrigger by remember { mutableStateOf(0) }

    val eventsOnSelectedDate by remember(selectedDate, eventList, refreshTrigger) {
        derivedStateOf {
            eventList.filter { it.arrival.toLocalDate() == selectedDate }
        }
    }

    var editingEvent by remember { mutableStateOf<EventDTO?>(null) }

    val calendarTypography = Typography(
        body1 = TextStyle(
            color = MaterialTheme.colors.secondary,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
        ),
        h1 = TextStyle(
            color = MaterialTheme.colors.secondary,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
        ),
        h4 = TextStyle(
            color = Colors.Bittersweet,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
        ),
        h5 = TextStyle(
            color = MaterialTheme.colors.secondary,
            fontFamily = ubuntuFontFamily,
            fontSize = 24.sp,
        ),
        h6 = TextStyle(
            color = MaterialTheme.colors.secondary,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
        )
    )

    @Composable
    fun Calendar(inset: PaddingValues) = MaterialTheme(typography = calendarTypography) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colors.primary)
                .padding(inset)
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
                            eventsOnSelectedDate.forEach { event ->
                                item {
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        elevation = CardDefaults.cardElevation(
                                            defaultElevation = 4.dp
                                        ),
                                        modifier = Modifier
                                            .padding(vertical = 4.dp)
                                            .width(375.dp),
                                        onClick = { editingEvent = event }
                                    ) {
                                        Row(
                                            modifier = Modifier
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
                                                    .background(MaterialTheme.colors.primary)
                                                    .padding(16.dp)
                                            ) {
                                                Text(
                                                    text = event.name
                                                )
                                            }
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
            editingEvent?.let { event ->
                ModalViewEventSheet(event, {
                    editingEvent = null
                }) {
                    navigateCreateEvent(event)
                }
            }
        }
    }

    Scaffold(
        bottomBar = {
            NavBarV2(navigate, Screen.Calendar)
        }
    ) { insets ->
        Calendar(insets)
    }
}