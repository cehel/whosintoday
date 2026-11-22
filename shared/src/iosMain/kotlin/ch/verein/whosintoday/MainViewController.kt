package ch.verein.whosintoday

import androidx.compose.ui.window.ComposeUIViewController
import ch.verein.whosintoday.data.attendanceDatabasePath
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    val databasePath = attendanceDatabasePath()
    return ComposeUIViewController { App(databasePath) }
}
