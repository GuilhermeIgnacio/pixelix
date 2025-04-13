package com.daniebeler.pfpixelix.utils

import coil3.PlatformContext
import io.github.vinceglb.filekit.PlatformFile
import platform.Foundation.NSURL
import platform.UIKit.UIViewController

private data class IosUri(override val url: NSURL) : KmpUri() {
    override fun toString(): String = url.toString()
}

actual abstract class KmpUri {
    abstract val url: NSURL
    actual abstract override fun toString(): String
}
actual val EmptyKmpUri: KmpUri = IosUri(NSURL(string = ""))
actual fun KmpUri.getPlatformUriObject(): Any = url
actual fun String.toKmpUri(): KmpUri = IosUri(NSURL(string = this))
actual fun PlatformFile.toKmpUri(): KmpUri = IosUri(nsUrl)

actual abstract class KmpContext {
    abstract val viewController: UIViewController
}
actual val KmpContext.coilContext get() = PlatformContext.INSTANCE
