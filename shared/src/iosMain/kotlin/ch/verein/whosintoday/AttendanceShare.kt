package ch.verein.whosintoday

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.create
import platform.Foundation.writeToFile
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIViewController
import platform.UIKit.popoverPresentationController

@OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)
internal fun UIViewController.shareAttendanceCsv(filename: String, bytes: ByteArray): Boolean {
    val anchor = view
    if (anchor.window == null || presentedViewController != null) return false

    val files = NSFileManager.defaultManager
    val directory = NSTemporaryDirectory() + NSUUID().UUIDString
    if (!files.createDirectoryAtPath(directory, withIntermediateDirectories = true, attributes = null, error = null)) {
        return false
    }

    val path = "$directory/$filename"
    val written = bytes.usePinned {
        NSData.create(bytes = it.addressOf(0), length = bytes.size.toULong()).writeToFile(path, atomically = true)
    }
    if (!written) {
        files.removeItemAtPath(directory, error = null)
        return false
    }

    val sheet = UIActivityViewController(
        activityItems = listOf(NSURL.fileURLWithPath(path)),
        applicationActivities = null,
    )
    sheet.popoverPresentationController?.apply {
        sourceView = anchor
        sourceRect = anchor.bounds
    }
    sheet.completionWithItemsHandler = { _, _, _, _ ->
        files.removeItemAtPath(directory, error = null)
    }
    presentViewController(sheet, animated = true, completion = null)
    return true
}
