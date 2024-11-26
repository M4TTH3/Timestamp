package org.timestamp.mobile

// Imports...
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.Divider
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.auth.FirebaseUser
import org.timestamp.mobile.ui.theme.Colors

@Composable
fun SettingsScreen(
    currentUser: FirebaseUser?,
    onSignOutClick: () -> Unit
) {
    // Set up common style formatting
    val ubuntuFontFamily = FontFamily(
        Font(R.font.ubuntu_regular),  // Regular
        Font(R.font.ubuntu_bold, FontWeight.Bold)  // Bold
    )
    val textStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp
    )

    // Maintains state of whether we should be in light mode or dark mode
    var darkModeOn by remember { mutableStateOf(false) }
    val sliderPosition by remember { mutableStateOf(0f) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Colors.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.size(70.dp))
            Text(text = "Settings",
                color = Colors.Black,
                fontFamily = ubuntuFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
            )
            Spacer(modifier = Modifier.size(30.dp))
            Divider(
                color = Colors.Platinum
            )
            Spacer(modifier = Modifier.size(30.dp))
            // Account Information Section
            Text(text = "Account Information",
                color = Colors.Black,
                fontFamily = ubuntuFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
            )
            Spacer(modifier = Modifier.size(20.dp))
            Divider(
                color = Colors.Platinum
            )
            Spacer(modifier = Modifier.size(20.dp))
            // Display user details here
            Row() {
                Column() {
                    Text(text = "Name",
                        color = Colors.Black,
                        fontFamily = ubuntuFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    currentUser?.let { user ->
                        user.displayName?.let { name ->
                            Text(
                                text = name,
                                color = Colors.Black,
                                fontFamily = ubuntuFontFamily,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.size(25.dp))
                    }
                    // Allows user to sign out from the settings page
                    Button(
                        onClick = {
                            Log.d("SettingsScreen","signed out")
                            onSignOutClick() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Colors.Black
                        )) {
                        androidx.compose.material3.Text(
                            text = "Sign Out",
                            style = textStyle.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Colors.White
                        )
                    }
                    Spacer(modifier = Modifier.height(50.dp))
                }
                Spacer(modifier = Modifier.width(100.dp))
                // User profile picture
                Column() {
                    currentUser?.let { user ->
                        user.photoUrl?.let {
                            AsyncImage(
                                modifier = Modifier
                                    .size(140.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(it)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "profile picture",
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
            Divider(
                color = Colors.Platinum
            )
            Spacer(modifier = Modifier.size(20.dp))
            // Account Preferences section
            Text(text = "Account Preferences",
                color = Colors.Black,
                fontFamily = ubuntuFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
            )
            Spacer(modifier = Modifier.size(20.dp))
            Divider(
                color = Colors.Platinum
            )
            Spacer(modifier = Modifier.size(20.dp))
            // NOT YET IMPLEMENTED: Switch between dark and light mode
            Row() {
                Text(text = if (darkModeOn) "Dark Mode" else "Light Mode",
                    color = Colors.Black,
                    fontFamily = ubuntuFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.width(150.dp)
                )
                Spacer(modifier = Modifier.width(150.dp))
                Switch(
                    checked = darkModeOn,
                    onCheckedChange = {
                        darkModeOn = it
                        Colors.setThemeColors(darkModeOn) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Colors.Black,
                        uncheckedThumbColor = Colors.White
                    ),
                    modifier = Modifier.scale(1.3f)
                )

            }
        }
    }
}