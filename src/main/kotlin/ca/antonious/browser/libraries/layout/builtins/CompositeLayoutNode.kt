package ca.antonious.browser.libraries.layout.builtins

import ca.antonious.browser.libraries.graphics.core.Canvas
import ca.antonious.browser.libraries.graphics.core.subRegion
import ca.antonious.browser.libraries.layout.core.LayoutNode

abstract class CompositeLayoutNode : LayoutNode() {

    protected val children = mutableListOf<LayoutNode>()

    fun addChild(node: LayoutNode) {
        this.children += node
    }

    fun setChildren(children: List<LayoutNode>) {
        this.children.clear()
        this.children.addAll(children)
    }

    override fun onDrawTo(canvas: Canvas) {
        children.forEach {
            it.drawTo(canvas.subRegion(it.frame))
        }
    }
}
