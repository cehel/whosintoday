package ch.verein.whosintoday

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ch.verein.whosintoday.data.attendanceDatabasePath

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val databasePath = attendanceDatabasePath(applicationContext)

        setContent {
            App(databasePath)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}