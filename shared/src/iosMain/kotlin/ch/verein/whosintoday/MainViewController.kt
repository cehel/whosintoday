package ch.verein.whosintoday

import androidx.compose.ui.uikit.LocalUIViewController
import androidx.compose.ui.window.ComposeUIViewController
import ch.verein.whosintoday.data.attendanceDatabasePath
import platform.UIKit.UIViewController

// Preserves the factory name used by the Swift app.
@Suppress("ktlint:standard:function-naming")
fun MainViewController(): UIViewController {
    val databasePath = attendanceDatabasePath()
    return ComposeUIViewController {
        val controller = LocalUIViewController.current
        App(databasePath, controller::shareAttendanceCsv)
    }
}
