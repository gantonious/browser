package ca.antonious.browser.libraries.javascript.interpreter.tests

import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.interpreter.builtins.array.JavascriptArray
import java.io.File

fun main() {
    val interpreter = JavascriptInterpreter()
    interpreter.interpret(File("TestUtils.js"))
    interpreter.interpret(File("Tests.js"))


    val results = interpreter.interpret("__testContext.results").toTestResults()
    printTestResults(results)
}

data class TestResult(
    val passed: Boolean,
    val name: String,
    val failureMessage: String?,
    val runtime: Long
)

private fun JavascriptValue.toTestResults(): List<TestResult> {
    val array = valueAs<JavascriptValue.Object>()!!.value as JavascriptArray
    return array.array.map {
        val obj = it.valueAs<JavascriptValue.Object>()!!.value
        TestResult(
            passed = obj.getProperty("status").valueAs<JavascriptValue.String>()!!.value == "pass",
            name = obj.getProperty("testName").valueAs<JavascriptValue.String>()!!.value,
            failureMessage = obj.getProperty("message").valueAs<JavascriptValue.String>()?.value,
            runtime = obj.getProperty("runTime").valueAs<JavascriptValue.Number>()!!.value.toLong()
        )
    }
}

private fun printTestResults(testResults: List<TestResult>) {
    val passedTests = testResults.filter { it.passed }
    val failedTests = testResults.filterNot { it.passed }
    val passCount = testResults.size - failedTests.size

    passedTests.forEach { pass ->
        println("${" PASS ".greenBackground()} ${pass.name} (${pass.runtime}ms)")
    }

    failedTests.forEach { failure ->
        println("${" FAIL ".redBackground()} ${failure.name} (${failure.runtime}ms)")
        val failureLines = "       " + (failure.failureMessage ?: "").split("\\n").joinToString("\n       ")
        println(failureLines)
    }

    println()
    print("Tests: ")
    
    if (passCount > 0) {
        print("$passCount passed ".green())
    }

    if (failedTests.isNotEmpty()) {
        print("${failedTests.size} failed".red())
    }

    println()
}

object ANSICode {
    const val redBackground = "\u001B[30;41m"
    const val greenBackground = "\u001B[30;42m"
    const val red = "\u001B[31m"
    const val green = "\u001B[32m"
    const val reset = "\u001B[0m"
}

fun String.red(): String {
    return "${ANSICode.red}$this${ANSICode.reset}"
}

fun String.green(): String {
    return "${ANSICode.green}$this${ANSICode.reset}"
}

fun String.redBackground(): String {
    return "${ANSICode.redBackground}$this${ANSICode.reset}"
}

fun String.greenBackground(): String {
    return "${ANSICode.greenBackground}$this${ANSICode.reset}"
}
