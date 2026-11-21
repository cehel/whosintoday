package ch.verein.whosintoday

import androidx.compose.ui.window.ComposeUIViewController
import ch.verein.whosintoday.data.initializeAttendanceDatabase
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    initializeAttendanceDatabase()
    return ComposeUIViewController { App() }
}
