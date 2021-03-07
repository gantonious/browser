package ca.antonious.browser.libraries.javascript.lexer

class PrettyList<T> : ArrayList<T>() {
    override fun toString(): String {
        val tab = "    "
        return "[\n$tab${joinToString(separator = ",\n$tab")}\n]"
    }
}

fun <T> List<T>.toPrettyList(): List<T> {
    val prettyList = PrettyList<T>()
    prettyList.addAll(this)
    return prettyList
}