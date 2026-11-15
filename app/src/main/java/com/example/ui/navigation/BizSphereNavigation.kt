package com.example.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.core.AppLanguage
import com.example.core.LocalAppStrings
import com.example.core.ThemeMode
import com.example.ui.screens.BackupScreen
import com.example.ui.screens.CashbookScreen
import com.example.ui.screens.CustomerDetailScreen
import com.example.ui.screens.CustomersScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DueRemindersScreen
import com.example.ui.screens.InvoicesScreen
import com.example.ui.screens.MoreHubScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.viewmodel.MainUiState
import com.example.ui.viewmodel.MainViewModel

sealed class BizSphereDestination(val route: String) {
    data object Dashboard : BizSphereDestination("dashboard")
    data object CustomerList : BizSphereDestination("customers")
    data object Transactions : BizSphereDestination("transactions")
    data object Invoices : BizSphereDestination("invoices")
    data object More : BizSphereDestination("more")
    data object Cashbook : BizSphereDestination("cashbook")
    data object DueReminders : BizSphereDestination("due_reminders")
    data object Reports : BizSphereDestination("reports")
    data object Backup : BizSphereDestination("backup")
    data object Settings : BizSphereDestination("settings")
    data object CustomerDetail : BizSphereDestination("customer_detail/{customerId}") {
        const val ARG_CUSTOMER_ID = "customerId"
        fun createRoute(customerId: Long): String = "customer_detail/$customerId"
    }
}

private data class TopLevelNavItem(
    val destination: BizSphereDestination,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun BizSphereNavShell(
    state: MainUiState,
    viewModel: MainViewModel,
    navController: NavHostController = rememberNavController()
) {
    val strings = LocalAppStrings.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    var openInvoiceBuilder by remember { mutableStateOf(false) }

    val topLevelItems = listOf(
        TopLevelNavItem(
            destination = BizSphereDestination.Dashboard,
            label = strings.navDashboard,
            selectedIcon = Icons.Rounded.Home,
            unselectedIcon = Icons.Outlined.Home,
            testTag = "nav_dashboard"
        ),
        TopLevelNavItem(
            destination = BizSphereDestination.CustomerList,
            label = strings.navCustomers,
            selectedIcon = Icons.Rounded.Book,
            unselectedIcon = Icons.Outlined.Book,
            testTag = "nav_customers"
        ),
        TopLevelNavItem(
            destination = BizSphereDestination.Transactions,
            label = strings.allTransactions.split(" ").first(),
            selectedIcon = Icons.Rounded.History,
            unselectedIcon = Icons.Outlined.History,
            testTag = "nav_transactions"
        ),
        TopLevelNavItem(
            destination = BizSphereDestination.Invoices,
            label = strings.navInvoices,
            selectedIcon = Icons.Rounded.ReceiptLong,
            unselectedIcon = Icons.Outlined.ReceiptLong,
            testTag = "nav_invoices"
        ),
        TopLevelNavItem(
            destination = BizSphereDestination.More,
            label = strings.navMore,
            selectedIcon = Icons.Rounded.GridView,
            unselectedIcon = Icons.Outlined.GridView,
            testTag = "nav_more"
        )
    )

    val topLevelRoutes = remember { topLevelItems.map { it.destination.route }.toSet() }
    val showTopLevelNav = currentDestination?.route in topLevelRoutes

    fun navigateTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 680.dp

        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (showTopLevelNav && !useRail) {
                    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                        topLevelItems.forEach { item ->
                            val selected = currentDestination?.hierarchy?.any {
                                it.route == item.destination.route
                            } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTopLevel(item.destination.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label
                                    )
                                },
                                label = { Text(item.label, maxLines = 1) },
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (showTopLevelNav && useRail) {
                    NavigationRail(containerColor = MaterialTheme.colorScheme.surface) {
                        topLevelItems.forEach { item ->
                            val selected = currentDestination?.hierarchy?.any {
                                it.route == item.destination.route
                            } == true
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTopLevel(item.destination.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label
                                    )
                                },
                                label = { Text(item.label) },
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                    }
                }

                BizSphereNavGraph(
                    navController = navController,
                    state = state,
                    viewModel = viewModel,
                    openInvoiceBuilder = openInvoiceBuilder,
                    onOpenInvoiceBuilder = { openInvoiceBuilder = true },
                    onConsumeOpenInvoiceBuilder = { openInvoiceBuilder = false },
                    onNavigateTopLevel = ::navigateTopLevel,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun BizSphereNavGraph(
    navController: NavHostController,
    state: MainUiState,
    viewModel: MainViewModel,
    openInvoiceBuilder: Boolean,
    onOpenInvoiceBuilder: () -> Unit,
    onConsumeOpenInvoiceBuilder: () -> Unit,
    onNavigateTopLevel: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = BizSphereDestination.Dashboard.route,
        enterTransition = { fadeIn(animationSpec = tween(200)) },
        exitTransition = { fadeOut(animationSpec = tween(180)) },
        modifier = modifier
    ) {
        composable(BizSphereDestination.Dashboard.route) {
            DashboardScreen(
                state = state,
                onToggleLanguage = {
                    val next = if (state.prefs.language == AppLanguage.EN) AppLanguage.BN else AppLanguage.EN
                    viewModel.setLanguage(next)
                },
                onCycleTheme = {
                    val next = when (state.prefs.themeMode) {
                        ThemeMode.LIGHT -> ThemeMode.DARK
                        ThemeMode.DARK -> ThemeMode.LIGHT
                        ThemeMode.SYSTEM -> ThemeMode.DARK
                    }
                    viewModel.setThemeMode(next)
                },
                onLockApp = { viewModel.lockAppNow() },
                onNavigateCustomers = { onNavigateTopLevel(BizSphereDestination.CustomerList.route) },
                onOpenCustomerDetail = { id ->
                    navController.navigate(BizSphereDestination.CustomerDetail.createRoute(id))
                },
                onNavigateCashbook = {
                    navController.navigate(BizSphereDestination.Cashbook.route)
                },
                onNavigateInvoices = { onNavigateTopLevel(BizSphereDestination.Invoices.route) },
                onNavigateNewInvoice = {
                    onOpenInvoiceBuilder()
                    onNavigateTopLevel(BizSphereDestination.Invoices.route)
                },
                onNavigateTransactions = {
                    onNavigateTopLevel(BizSphereDestination.Transactions.route)
                },
                onNavigateDueReminders = {
                    navController.navigate(BizSphereDestination.DueReminders.route)
                },
                onNavigateReports = {
                    navController.navigate(BizSphereDestination.Reports.route)
                }
            )
        }

        composable(BizSphereDestination.CustomerList.route) {
            CustomersScreen(
                state = state,
                onOpenCustomerDetail = { id ->
                    navController.navigate(BizSphereDestination.CustomerDetail.createRoute(id))
                },
                onSaveCustomer = { viewModel.saveCustomer(it) }
            )
        }

        composable(
            route = BizSphereDestination.CustomerDetail.route,
            arguments = listOf(
                navArgument(BizSphereDestination.CustomerDetail.ARG_CUSTOMER_ID) {
                    type = NavType.LongType
                }
            ),
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(240))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(240))
            }
        ) { backStack ->
            val custId = backStack.arguments?.getLong(BizSphereDestination.CustomerDetail.ARG_CUSTOMER_ID) ?: 0L
            CustomerDetailScreen(
                customerId = custId,
                state = state,
                onBack = { navController.popBackStack() },
                onSaveCustomer = { viewModel.saveCustomer(it) },
                onDeleteCustomer = { viewModel.deleteCustomer(it) },
                onUpdateDueReminder = { id, due -> viewModel.updateCustomerDueReminder(id, due) },
                onSaveKhataEntry = { viewModel.saveKhataEntry(it) },
                onDeleteKhataEntry = { viewModel.deleteKhataEntry(it) }
            )
        }

        composable(BizSphereDestination.Transactions.route) {
            TransactionsScreen(
                state = state,
                onBack = null,
                onOpenCustomerDetail = { id ->
                    navController.navigate(BizSphereDestination.CustomerDetail.createRoute(id))
                }
            )
        }

        composable(BizSphereDestination.Cashbook.route) {
            CashbookScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onSaveEntry = { viewModel.saveCashbookEntry(it) },
                onDeleteEntry = { viewModel.deleteCashbookEntry(it) }
            )
        }

        composable(BizSphereDestination.Invoices.route) {
            InvoicesScreen(
                state = state,
                openBuilderInitially = openInvoiceBuilder,
                onConsumeOpenBuilder = onConsumeOpenInvoiceBuilder,
                generateNextInvoiceNumber = { viewModel.getNextInvoiceNumber() },
                onSaveInvoice = { inv, items, sync, onSaved ->
                    viewModel.saveInvoice(inv, items, sync, onSaved)
                },
                onDeleteInvoice = { viewModel.deleteInvoice(it) }
            )
        }

        composable(BizSphereDestination.More.route) {
            MoreHubScreen(
                state = state,
                onNavigateCashbook = { navController.navigate(BizSphereDestination.Cashbook.route) },
                onNavigateTransactions = { onNavigateTopLevel(BizSphereDestination.Transactions.route) },
                onNavigateDueReminders = { navController.navigate(BizSphereDestination.DueReminders.route) },
                onNavigateReports = { navController.navigate(BizSphereDestination.Reports.route) },
                onNavigateBackup = { navController.navigate(BizSphereDestination.Backup.route) },
                onNavigateSettings = { navController.navigate(BizSphereDestination.Settings.route) }
            )
        }

        composable(BizSphereDestination.DueReminders.route) {
            DueRemindersScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onOpenCustomerDetail = { id ->
                    navController.navigate(BizSphereDestination.CustomerDetail.createRoute(id))
                },
                onRecordPayment = { viewModel.saveKhataEntry(it) }
            )
        }

        composable(BizSphereDestination.Reports.route) {
            ReportsScreen(
                state = state,
                onBack = { navController.popBackStack() }
            )
        }

        composable(BizSphereDestination.Backup.route) {
            BackupScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onExportBackup = { uri, cb -> viewModel.exportBackupToUri(context, uri, cb) },
                onRestoreBackup = { uri, cb -> viewModel.restoreBackupFromUri(context, uri, cb) },
                onSeedDemo = { viewModel.seedDemoData() },
                onClearAll = { viewModel.clearAllData() }
            )
        }

        composable(BizSphereDestination.Settings.route) {
            SettingsScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onUpdateProfile = { name, owner, phone, addr ->
                    viewModel.updateBusinessProfile(name, owner, phone, addr)
                },
                onSetLanguage = { viewModel.setLanguage(it) },
                onSetTheme = { viewModel.setThemeMode(it) },
                onSetCurrency = { viewModel.setCurrencySymbol(it) },
                onSetPin = { viewModel.setPin(it) },
                onDisablePin = { viewModel.disablePin() },
                onLockAppNow = { viewModel.lockAppNow() },
                onLogout = { viewModel.logout() }
            )
        }
    }
}
