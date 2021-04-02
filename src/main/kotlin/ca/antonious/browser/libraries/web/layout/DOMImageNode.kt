package ca.antonious.browser.libraries.web.layout

import ca.antonious.browser.libraries.graphics.core.Bitmap
import ca.antonious.browser.libraries.graphics.core.Canvas
import ca.antonious.browser.libraries.graphics.core.MeasuringTape
import ca.antonious.browser.libraries.graphics.core.Size
import ca.antonious.browser.libraries.graphics.images.ImageLoader
import ca.antonious.browser.libraries.graphics.images.Result
import ca.antonious.browser.libraries.html.HtmlElement

class DOMImageNode(
    parent: DOMParentLayoutNode?,
    imgNode: HtmlElement.Node,
    resolvedUrl: String,
    imageLoader: ImageLoader
) : DOMLayoutNode(parent, imgNode) {

    private var bitmap: Bitmap? = null

    init {
        imageLoader.loadImage(resolvedUrl) {
            when (it) {
                is Result.Success -> bitmap = it.value
                is Result.Failure -> Unit
            }
        }
    }

    override fun measure(measuringTape: MeasuringTape, widthConstraint: Float, heightConstraint: Float): Size {
        return bitmap?.let { Size(it.width.toFloat(), it.height.toFloat()) } ?: Size(0f, 0f)
    }

    override fun drawTo(canvas: Canvas) {
        bitmap?.let {
            canvas.drawBitmap(it, 0f, 0f)
        }
    }
}
