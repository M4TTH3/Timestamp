package org.timestamp.mobile

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import org.timestamp.lib.dto.EventDTO
import org.timestamp.mobile.ui.elements.BackgroundLocationDialog
import org.timestamp.mobile.ui.screens.*
import org.timestamp.mobile.ui.theme.Colors
import org.timestamp.mobile.ui.theme.TimestampTheme
import org.timestamp.mobile.utility.PermissionProvider
import org.timestamp.mobile.utility.Screen
import org.timestamp.mobile.viewmodels.EventViewModel
import org.timestamp.mobile.viewmodels.ThemeViewModel

class MainNavController(
    private val activity: TimestampActivity
) {
    private val auth = FirebaseAuth.getInstance()
    private val eventViewModel: EventViewModel by activity.viewModels()
    private val themeViewModel: ThemeViewModel by activity.viewModels()
    private val credentialManager: CredentialManager = CredentialManager.create(activity)
    private val signInWithGoogleOption: GetSignInWithGoogleOption = GetSignInWithGoogleOption
        .Builder(activity.getString(R.string.web_client_id))
        .build()
    private val request: GetCredentialRequest = GetCredentialRequest.Builder()
        .addCredentialOption(signInWithGoogleOption)
        .build()

    /**
     * Initialize auth login with firebase, prompting a user flow if required
     */
    private suspend fun handleSignIn(
        navController : NavController
    ) {
        try {
            val result = credentialManager.getCredential(
                request = request,
                context = activity
            )
            val credential = result.credential

            if (credential !is CustomCredential ||
                credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                Log.e("Sign In", "Wrong token type!")
                return
            }

            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val firebaseCredential = GoogleAuthProvider
                .getCredential(googleIdTokenCredential.idToken, null)

            auth.signInWithCredential(firebaseCredential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.i("Sign In", "Successfully logged in")
                    eventViewModel.pingBackend()
                    navController.navigate(Screen.Home.name)
                }
            }
        } catch (e: GoogleIdTokenParsingException) {
            Log.e("Sign In", "Received an invalid google id token response", e)
        } catch (e: GetCredentialException) {
            Log.e("Sign In", e.toString())
        }
    }

    /**
     * Main composable for navigation features. Includes credential management.
     * Sets up Credential Manager.
     */
    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun TimestampNavController() {
        val permissions = rememberMultiplePermissionsState(PermissionProvider.PERMISSIONS)
        val bgPermission = rememberPermissionState(PermissionProvider.BACKGROUND_LOCATION)
        val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
        val navController = rememberNavController()
        val scope = rememberCoroutineScope()
        val startDestination = remember { if (auth.currentUser == null) Screen.Login.name else Screen.Home.name }

        LaunchedEffect(Unit) {
            if (!permissions.allPermissionsGranted) navController.navigate(startDestination)
            permissions.launchMultiplePermissionRequest()
        }

        fun signIn() {
            scope.launch {
                handleSignIn(navController)
            }
        }
        fun signOut() {
            auth.signOut()
            eventViewModel.stopGetEventsPolling()
            clearLocationService()
            scope.launch {
                credentialManager.clearCredentialState(
                    ClearCredentialStateRequest()
                )
                navController.popBackStack()
                navController.navigate(Screen.Login.name)
            }
        }

        fun navigateCreateEvent(event: EventDTO?) {
            eventViewModel.setViewEvent(event)
            navController.navigate(Screen.CreateOrEditEvent.name)
        }

        fun navigate(screen: Screen) {
            navController.navigate(screen.name)
        }

        TimestampTheme(darkTheme = isDarkTheme) {
            NavHost(
                navController = navController,
                startDestination = startDestination
            ) {
                composable(
                    Screen.Login.name,
                ) {
                    LoginScreen(
                        onSignInClick = ::signIn
                    )
                }
                composable(Screen.Home.name) {
                    // False if all permissions granted
                    val permissionGranted = permissions.allPermissionsGranted
                    val showRationale = permissions.shouldShowRationale
                    val bgPermissionGranted = bgPermission.status.isGranted
                    val showBackgroundRationale = remember { mutableStateOf(!bgPermissionGranted) }

                    LaunchedEffect(permissionGranted) {
                        if (permissionGranted) startLocationService()
                        else clearLocationService()
                    }

                    HomeScreen(
                        onSignOutClick = ::signOut,
                        onContinueClick = {
                            if (permissionGranted) navController.navigate(Screen.Events.name)
                            else if (showRationale) permissions.launchMultiplePermissionRequest()
                            else {
                                // Need the user to update manually in settings
                                // after many denials
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", activity.packageName, null)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                activity.startActivity(intent)
                            }
                        },
                        continueText = if (permissionGranted) "Continue" else "Settings",
                        warningText = if (permissionGranted) null else "Please enable all permissions"
                    )

                    if (permissionGranted && showBackgroundRationale.value) {
                        BackgroundLocationDialog(
                            onAllow = {
                                bgPermission.launchPermissionRequest()
                                showBackgroundRationale.value = false
                            },
                            onDeny = { showBackgroundRationale.value = false }
                        )
                    }
                }
                composable(Screen.Events.name) {
                    EventHomeScreen(
                        navigateCreateEvent = ::navigateCreateEvent,
                        navigate = ::navigate
                    )
                }
                composable(Screen.Calendar.name) {
                    CalendarScreen(
                        navigateCreateEvent = ::navigateCreateEvent,
                        navigate = ::navigate
                    )
                }
                composable(Screen.Settings.name) {
                    SettingsScreen(
                        currentUser = auth.currentUser,
                        onSignOutClick = ::signOut
                    )
                    NavBar(navController = navController, currentScreen = "Settings")
                }

                composable(Screen.CreateOrEditEvent.name) {
                    // Navigation to Create or Edit an Event
                    CreateEventScreen(navController::popBackStack)
                }

                composable(Screen.MapView.name) {
                    MapScreen()
                }
            }
        }
    }

    /**
     * Composable for navigation bar at the bottom
     */
    @Composable
    fun NavBar(navController: NavController, currentScreen : String) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(Color(0xFFFF6F61))
                    .align(Alignment.BottomCenter)
                    .offset(y = (-2).dp)
            ) {
                Box (
                    modifier = Modifier
                        .height(3.dp)
                        .fillMaxWidth()
                        .background(MaterialTheme.colors.secondary)
                        .align(Alignment.TopCenter)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Box() {}
                    IconButton(
                        onClick = {
                            navController.navigate(Screen.Events.name)
                        },
                        modifier = Modifier
                            .padding(8.dp)
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Home,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = if (currentScreen == "Events") Color.Black else Color(0xFF522D2A)
                        )
                    }
                    IconButton(
                        onClick = {
                            navController.navigate(Screen.Calendar.name)
                        },
                        modifier = Modifier
                            .padding(vertical = 8.dp, horizontal = 20.dp)
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = if (currentScreen == "Calendar") Color.Black else Color(0xFF522D2A)
                        )
                    }
                    IconButton(
                        onClick = {
                            navController.navigate(Screen.Settings.name)
                        },
                        modifier = Modifier
                            .padding(8.dp)
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = if (currentScreen == "Settings") Color.Black else Color(0xFF522D2A)
                        )
                    }
                    Box {}
                }
            }
        }
    }

    private fun startLocationService() {
        val serviceIntent = Intent(activity, TimestampService::class.java)
        activity.startService(serviceIntent)
    }

    private fun clearLocationService() {
        val serviceIntent = Intent(activity, TimestampService::class.java)
        activity.stopService(serviceIntent)
    }
}

@Composable
fun NavBarV2(
    navigate: (Screen) -> Unit,
    currentScreen : Screen
) {
    val eventScreen = Screen.Events
    val calendarScreen = Screen.Calendar
    val settingScreen = Screen.Settings

    require (currentScreen in listOf(eventScreen, calendarScreen, settingScreen)) {
        "Invalid current screen: $currentScreen"
    }

    @Composable
    fun NavItemColour() = NavigationBarItemDefaults.colors(
        indicatorColor = Color.Transparent
    )

    NavigationBar(
        containerColor = Colors.Bittersweet
    ) {
        listOf(
            Pair(eventScreen, Icons.Filled.Home),
            Pair(calendarScreen, Icons.Filled.CalendarMonth),
            Pair(settingScreen, Icons.Filled.Settings)
        ).map { (screen, icon) ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = if (currentScreen == screen) Color.Black else Colors.BlackFaint
                    )
                },
                selected = currentScreen == screen,
                onClick = { navigate(screen) },
                colors = NavItemColour()
            )

        }
    }
}

fun getUser() : FirebaseUser? = FirebaseAuth.getInstance().currentUser