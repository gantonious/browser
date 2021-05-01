package ca.antonious.browser.libraries.shared.parsing

data class SourceInfo(
    val line: Int,
    val column: Int,
    val filename: String = "unknown",
    val source: String = ""
) {
    companion object {
        fun unknown(): SourceInfo {
            return SourceInfo(0, 0)
        }
    }
}
