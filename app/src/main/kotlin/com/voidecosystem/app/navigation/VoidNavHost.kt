package com.voidecosystem.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.voidecosystem.feature.automation.AutomationDestination
import com.voidecosystem.feature.automation.AutomationRoute
import com.voidecosystem.feature.browser.BrowserDestination
import com.voidecosystem.feature.browser.BrowserRoute
import com.voidecosystem.feature.calculator.CalculatorDestination
import com.voidecosystem.feature.calculator.CalculatorRoute
import com.voidecosystem.feature.calendar.CalendarDestination
import com.voidecosystem.feature.calendar.CalendarRoute
import com.voidecosystem.feature.dashboard.DashboardDestination
import com.voidecosystem.feature.dashboard.DashboardRoute
import com.voidecosystem.feature.dialer.DialerDestination
import com.voidecosystem.feature.dialer.DialerRoute
import com.voidecosystem.feature.filemanager.FilemanagerDestination
import com.voidecosystem.feature.filemanager.FilemanagerRoute
import com.voidecosystem.feature.finance.FinanceDestination
import com.voidecosystem.feature.finance.FinanceRoute
import com.voidecosystem.feature.focushub.FocushubDestination
import com.voidecosystem.feature.focushub.FocushubRoute
import com.voidecosystem.feature.gallery.GalleryDestination
import com.voidecosystem.feature.gallery.GalleryRoute
import com.voidecosystem.feature.journal.JournalDestination
import com.voidecosystem.feature.journal.JournalRoute
import com.voidecosystem.feature.musicplayer.MusicplayerDestination
import com.voidecosystem.feature.musicplayer.MusicplayerRoute
import com.voidecosystem.feature.notes.NotesDestination
import com.voidecosystem.feature.notes.NotesRoute
import com.voidecosystem.feature.omniassistant.OmniassistantDestination
import com.voidecosystem.feature.omniassistant.OmniassistantRoute
import com.voidecosystem.feature.pantry.PantryDestination
import com.voidecosystem.feature.pantry.PantryRoute
import com.voidecosystem.feature.routines.RoutinesDestination
import com.voidecosystem.feature.routines.RoutinesRoute
import com.voidecosystem.feature.sysmonitor.SysmonitorDestination
import com.voidecosystem.feature.sysmonitor.SysmonitorRoute
import com.voidecosystem.feature.terminal.TerminalDestination
import com.voidecosystem.feature.terminal.TerminalRoute
import com.voidecosystem.feature.theming.ThemingDestination
import com.voidecosystem.feature.theming.ThemingRoute
import com.voidecosystem.feature.todo.TodoDestination
import com.voidecosystem.feature.todo.TodoRoute

/**
 * The single composition root for navigation: :app is the only module
 * that knows every feature module exists, so this is the one file that
 * has to change when a new pillar app is added to the ecosystem. Every
 * feature module itself stays unaware of its siblings.
 */
@Composable
fun VoidNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = DashboardDestination.ROUTE) {
        composable(DashboardDestination.ROUTE) {
            DashboardRoute(onModuleClick = { route -> navController.navigate(route) })
        }

        val back: () -> Unit = { navController.popBackStack() }

        composable(ThemingDestination.ROUTE) { ThemingRoute(onBack = back) }
        composable(TerminalDestination.ROUTE) { TerminalRoute(onBack = back) }
        composable(SysmonitorDestination.ROUTE) { SysmonitorRoute(onBack = back) }

        composable(OmniassistantDestination.ROUTE) { OmniassistantRoute(onBack = back) }
        composable(AutomationDestination.ROUTE) { AutomationRoute(onBack = back) }
        composable(RoutinesDestination.ROUTE) { RoutinesRoute(onBack = back) }

        composable(MusicplayerDestination.ROUTE) { MusicplayerRoute(onBack = back) }
        composable(GalleryDestination.ROUTE) { GalleryRoute(onBack = back) }

        composable(FocushubDestination.ROUTE) { FocushubRoute(onBack = back) }
        composable(TodoDestination.ROUTE) { TodoRoute(onBack = back) }
        composable(JournalDestination.ROUTE) { JournalRoute(onBack = back) }
        composable(FinanceDestination.ROUTE) { FinanceRoute(onBack = back) }
        composable(PantryDestination.ROUTE) { PantryRoute(onBack = back) }

        composable(CalculatorDestination.ROUTE) { CalculatorRoute(onBack = back) }
        composable(NotesDestination.ROUTE) { NotesRoute(onBack = back) }
        composable(CalendarDestination.ROUTE) { CalendarRoute(onBack = back) }
        composable(FilemanagerDestination.ROUTE) { FilemanagerRoute(onBack = back) }

        composable(DialerDestination.ROUTE) { DialerRoute(onBack = back) }
        composable(BrowserDestination.ROUTE) { BrowserRoute(onBack = back) }
    }
}
