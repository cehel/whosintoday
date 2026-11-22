package ch.verein.whosintoday.data

import androidx.test.platform.app.InstrumentationRegistry
import java.io.File

internal actual fun withTemporaryDatabase(test: (String) -> Unit) {
    val cache = InstrumentationRegistry.getInstrumentation().targetContext.cacheDir
    val file = File.createTempFile("attendance-", ".db", cache)
    try {
        test(file.absolutePath)
    } finally {
        file.delete()
    }
}
