package ca.antonious.browser.libraries.console

object ANSICode {
    const val redBackground = "\u001B[30;41m"
    const val greenBackground = "\u001B[30;42m"
    const val gray = "\u001B[38;5;240m"
    const val orange = "\u001B[38;5;214m"
    const val red = "\u001B[31m"
    const val green = "\u001B[32m"
    const val yellow = "\u001B[33m"
    const val magenta = "\u001B[35m"
    const val cyan = "\u001B[36m"
    const val reset = "\u001B[0m"
}

fun String.gray(): String {
    return "${ANSICode.gray}$this${ANSICode.reset}"
}

fun String.orange(): String {
    return "${ANSICode.orange}$this${ANSICode.reset}"
}

fun String.magenta(): String {
    return "${ANSICode.magenta}$this${ANSICode.reset}"
}

fun String.cyan(): String {
    return "${ANSICode.cyan}$this${ANSICode.reset}"
}

fun String.red(): String {
    return "${ANSICode.red}$this${ANSICode.reset}"
}

fun String.green(): String {
    return "${ANSICode.green}$this${ANSICode.reset}"
}

fun String.yellow(): String {
    return "${ANSICode.yellow}$this${ANSICode.reset}"
}

fun String.redBackground(): String {
    return "${ANSICode.redBackground}$this${ANSICode.reset}"
}

fun String.greenBackground(): String {
    return "${ANSICode.greenBackground}$this${ANSICode.reset}"
}
