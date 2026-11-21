package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.PersonAddAlt1
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.NursesRepository
import com.example.ui.NursesViewModel
import com.example.ui.auth.attemptAutoSignIn
import com.example.ui.auth.authStateFlow
import com.example.ui.auth.signOut
import com.example.ui.components.NurseDetailBottomSheet
import com.example.ui.screens.AdminAuthGate
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.DirectoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NurseListScreen
import com.example.ui.screens.NurseLoginScreen
import com.example.ui.screens.RegisterScreen
import com.example.ui.theme.NursesBDTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

object Routes {
    const val HOME = "home"
    const val NURSE_LOGIN = "nurse_login"
    const val DIRECTORY = "directory"
    const val NURSE_LIST = "nurse_list"
    const val REGISTER = "register"
    const val ADMIN = "admin"
}

private data class NavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NursesBDTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val auth = Firebase.auth
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    var preferredStartRoute by remember { mutableStateOf(Routes.DIRECTORY) }

    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {},
            onUnauthenticated = {},
            scope = scope
        )
    }

    val user = currentUser
    if (user == null) {
        GuestPortal(
            onAuthSuccessFromRoute = { targetRoute ->
                preferredStartRoute = targetRoute
            }
        )
    } else {
        key(user.uid) {
            val viewModel: NursesViewModel = viewModel(
                key = user.uid,
                factory = viewModelFactory {
                    initializer {
                        val app = checkNotNull(this[APPLICATION_KEY]) {
                            "APPLICATION_KEY missing from CreationExtras"
                        }
                        val databaseId = app.getString(R.string.firestore_database_id)
                        val db = FirebaseFirestore.getInstance(databaseId)
                        NursesViewModel(
                            repository = NursesRepository(db = db, auth = auth),
                            currentUserId = user.uid,
                            currentUserEmail = user.email ?: ""
                        )
                    }
                }
            )
            NursesBDApp(
                viewModel = viewModel,
                initialRoute = preferredStartRoute,
                onSignOut = {
                    preferredStartRoute = Routes.DIRECTORY
                    signOut(credentialManager = credentialManager, scope = scope)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GuestPortal(
    onAuthSuccessFromRoute: (String) -> Unit
) {
    val guestNavController = rememberNavController()
    val guestBackStack by guestNavController.currentBackStackEntryAsState()
    val guestRoute = guestBackStack?.destination?.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            if (guestRoute != null && guestRoute != Routes.HOME) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = when (guestRoute) {
                                Routes.NURSE_LOGIN -> "Nurse Sign-In"
                                Routes.REGISTER -> "Nurse Registration"
                                Routes.ADMIN -> "Admin Portal"
                                else -> "Nurses BD"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { guestNavController.popBackStack() },
                            modifier = Modifier.testTag("guest_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Home"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = guestNavController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onLoginClick = { guestNavController.navigate(Routes.NURSE_LOGIN) },
                    onRegisterClick = { guestNavController.navigate(Routes.REGISTER) },
                    onAdminClick = { guestNavController.navigate(Routes.ADMIN) }
                )
            }

            composable(Routes.NURSE_LOGIN) {
                NurseLoginScreen(
                    onAuthSuccess = { onAuthSuccessFromRoute(Routes.DIRECTORY) },
                    onNavigateToRegister = {
                        guestNavController.navigate(Routes.REGISTER)
                    }
                )
            }

            composable(Routes.REGISTER) {
                RegisterScreen(
                    isSignedIn = false,
                    onGoogleAuthSuccess = { onAuthSuccessFromRoute(Routes.REGISTER) },
                    onSubmitRegistration = { _, cb ->
                        cb(Result.failure(IllegalStateException("Sign in with Google first.")))
                    },
                    onNavigateAfterSuccess = {
                        guestNavController.popBackStack(Routes.HOME, inclusive = false)
                    },
                    successActionLabel = "Back to Home"
                )
            }

            composable(Routes.ADMIN) {
                AdminAuthGate(
                    isSignedIn = false,
                    onAuthSuccess = { onAuthSuccessFromRoute(Routes.ADMIN) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NursesBDApp(
    viewModel: NursesViewModel,
    initialRoute: String = Routes.DIRECTORY,
    onSignOut: () -> Unit
) {
    val directoryState by viewModel.directoryUiState.collectAsStateWithLifecycle()
    val nurseListState by viewModel.nurseListUiState.collectAsStateWithLifecycle()
    val analyticsState by viewModel.analyticsSummary.collectAsStateWithLifecycle()
    val adminSession by viewModel.adminSessionState.collectAsStateWithLifecycle()
    val allNurses by viewModel.allNurses.collectAsStateWithLifecycle()
    val selectedNurse by viewModel.selectedNurse.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val startDestination = remember(initialRoute) {
        when (initialRoute) {
            Routes.ADMIN, Routes.REGISTER, Routes.NURSE_LIST -> initialRoute
            else -> Routes.DIRECTORY
        }
    }

    val navItems = remember {
        listOf(
            NavItem(
                route = Routes.DIRECTORY,
                label = "Directory",
                selectedIcon = Icons.Filled.Groups,
                unselectedIcon = Icons.Outlined.Groups,
                testTag = "nav_directory"
            ),
            NavItem(
                route = Routes.NURSE_LIST,
                label = "All Nurses",
                selectedIcon = Icons.AutoMirrored.Filled.FormatListBulleted,
                unselectedIcon = Icons.AutoMirrored.Outlined.FormatListBulleted,
                testTag = "nav_nurse_list"
            ),
            NavItem(
                route = Routes.REGISTER,
                label = "Register",
                selectedIcon = Icons.Filled.PersonAddAlt1,
                unselectedIcon = Icons.Outlined.PersonAddAlt1,
                testTag = "nav_register"
            ),
            NavItem(
                route = Routes.ADMIN,
                label = "Admin",
                selectedIcon = Icons.Filled.AdminPanelSettings,
                unselectedIcon = Icons.Outlined.AdminPanelSettings,
                testTag = "nav_admin"
            )
        )
    }

    fun navigateTo(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Row(modifier = Modifier.fillMaxSize()) {
            if (useRail) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Spacer(Modifier.weight(0.1f))
                    navItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationRailItem(
                            selected = selected,
                            onClick = { navigateTo(item.route) },
                            icon = {
                                NavIconWithBadge(
                                    item = item,
                                    selected = selected,
                                    pendingCount = if (adminSession.isAuthorizedAdmin) analyticsState.totalPending else 0
                                )
                            },
                            label = { Text(item.label) },
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                    Spacer(Modifier.weight(1f))
                }
            }

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_nurse_emblem),
                                    contentDescription = null,
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Nurses BD",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        actions = {
                            TextButton(
                                onClick = onSignOut,
                                modifier = Modifier.testTag("session_logout_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = "Logout",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Logout")
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                },
                bottomBar = {
                    if (!useRail) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ) {
                            navItems.forEach { item ->
                                val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateTo(item.route) },
                                    icon = {
                                        NavIconWithBadge(
                                            item = item,
                                            selected = selected,
                                            pendingCount = if (adminSession.isAuthorizedAdmin) analyticsState.totalPending else 0
                                        )
                                    },
                                    label = { Text(item.label) },
                                    modifier = Modifier.testTag(item.testTag)
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = startDestination
                    ) {
                        composable(Routes.DIRECTORY) {
                            DirectoryScreen(
                                uiState = directoryState,
                                onSearchQueryChange = viewModel::updateSearchQuery,
                                onSelectDivision = viewModel::selectDivisionFilter,
                                onSelectSpecialty = viewModel::selectSpecialtyFilter,
                                onSelectBloodGroup = viewModel::selectBloodGroupFilter,
                                onToggleEmergencyOnly = viewModel::toggleEmergencyOnly,
                                onResetFilters = viewModel::resetFilters,
                                onSelectNurse = viewModel::openNurseDetail
                            )
                        }

                        composable(Routes.NURSE_LIST) {
                            NurseListScreen(
                                uiState = nurseListState,
                                onSearchQueryChange = viewModel::updateNurseListSearchQuery,
                                onSelectStatus = viewModel::selectNurseListStatus,
                                onSelectNurse = viewModel::openNurseDetail
                            )
                        }

                        composable(Routes.REGISTER) {
                            RegisterScreen(
                                isSignedIn = true,
                                signedInEmail = viewModel.currentUserEmail,
                                onSubmitRegistration = { nurse, cb ->
                                    viewModel.submitRegistration(nurse, cb)
                                },
                                onNavigateAfterSuccess = { navigateTo(Routes.DIRECTORY) },
                                successActionLabel = "View Directory"
                            )
                        }

                        composable(Routes.ADMIN) {
                            AdminScreen(
                                session = adminSession,
                                analytics = analyticsState,
                                allNurses = allNurses,
                                onSignOutAndSwitch = onSignOut,
                                onApproveNurse = viewModel::approveNurse,
                                onRejectNurse = viewModel::rejectNurse,
                                onDeleteNurse = viewModel::deleteNurse,
                                onAddAdmin = viewModel::addAdmin,
                                onRemoveAdmin = viewModel::removeAdmin,
                                onShowMessage = { msg ->
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(msg)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    selectedNurse?.let { nurse ->
        NurseDetailBottomSheet(
            nurse = nurse,
            onDismiss = viewModel::closeNurseDetail
        )
    }
}

@Composable
private fun NavIconWithBadge(
    item: NavItem,
    selected: Boolean,
    pendingCount: Int
) {
    val iconVector = if (selected) item.selectedIcon else item.unselectedIcon
    if (item.route == Routes.ADMIN && pendingCount > 0) {
        BadgedBox(
            badge = {
                Badge { Text("$pendingCount") }
            }
        ) {
            Icon(imageVector = iconVector, contentDescription = item.label)
        }
    } else {
        Icon(imageVector = iconVector, contentDescription = item.label)
    }
}
