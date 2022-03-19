package ca.antonious.browser.libraries.html

fun Char.isUpperHexDigit(): Boolean {
    return this.code in 'A'.code..'F'.code
}

fun Char.isLowerHexDigit(): Boolean {
    return this.code in 'a'.code..'f'.code
}