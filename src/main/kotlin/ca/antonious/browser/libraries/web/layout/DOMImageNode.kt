package ca.antonious.browser.libraries.web.layout

import ca.antonious.browser.libraries.graphics.core.Bitmap
import ca.antonious.browser.libraries.graphics.core.Canvas
import ca.antonious.browser.libraries.graphics.core.MeasuringTape
import ca.antonious.browser.libraries.graphics.core.Size
import ca.antonious.browser.libraries.graphics.images.ImageLoader
import ca.antonious.browser.libraries.graphics.images.Result
import ca.antonious.browser.libraries.html.HtmlElement
import kotlin.math.min

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
        return (
            bitmap?.let {
                Size(min(widthConstraint, it.width.toFloat()), min(heightConstraint, it.height.toFloat()))
            } ?: Size(0f, 0f)
        ).apply {
            frame.width = width
            frame.height = height
        }
    }

    override fun drawTo(canvas: Canvas) {
        bitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, frame.width, frame.height)
        }
    }
}
