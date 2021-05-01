package ca.antonious.browser.libraries.javascript.interpreter.testrunner

import ca.antonious.browser.libraries.console.green
import ca.antonious.browser.libraries.console.greenBackground
import ca.antonious.browser.libraries.console.red
import ca.antonious.browser.libraries.console.redBackground
import ca.antonious.browser.libraries.javascript.ast.JavascriptValue
import ca.antonious.browser.libraries.javascript.interpreter.JavascriptInterpreter
import ca.antonious.browser.libraries.javascript.interpreter.builtins.array.ArrayObject
import java.io.File
import kotlin.system.exitProcess

fun main() {
    val interpreter = JavascriptInterpreter()
    interpreter.interpret(File("TestUtils.js"))

    File("tests").listFilesRecursively().forEach {
        interpreter.interpret(it)
    }

    val results = interpreter.interpret("__testContext.results").toTestResults()
    printTestResults(results.sortedBy { it.name })
}

sealed class TestResult {
    abstract val passed: Boolean
    abstract val name: String
    abstract val runtime: Long

    data class TestSuite(
        override val name: String,
        override val runtime: Long,
        val tests: List<SingleTest>
    ) : TestResult() {
        override val passed: Boolean
            = !tests.any { !it.passed }
    }

    data class SingleTest(
        override val passed: Boolean,
        override val name: String,
        override val runtime: Long,
        val failureMessage: String?
    ) : TestResult()
}

private fun JavascriptValue.toTestResults(): List<TestResult> {
    val array = requireAsObject().asA<ArrayObject>()
    return array.array.map {
        val obj = it.requireAsObject()

        when (val type = obj.getProperty("type").requireAsString()) {
            "describe" -> {
                val describeName = obj.getProperty("name").requireAsString()
                val tests = obj.getProperty("results").toTestResults().filterIsInstance<TestResult.SingleTest>().map { test ->
                    test.copy(name = "$describeName ${test.name}")
                }

                TestResult.TestSuite(
                    name = "$describeName: ${tests.size} test(s)",
                    runtime = obj.getProperty("runTime").requireAsNumber().toLong(),
                    tests = tests
                )
            }
            "test" -> {
                TestResult.SingleTest(
                    passed = obj.getProperty("status").requireAsString() == "pass",
                    name = obj.getProperty("testName").requireAsString(),
                    failureMessage = obj.getProperty("message").asString(),
                    runtime = obj.getProperty("runTime").requireAsNumber().toLong()
                )
            }
            else -> error("Unknown test result type $type")
        }

    }
}

private fun printTestResults(testResults: List<TestResult>) {
    val allTests = testResults.flatMap {
        when (it) {
            is TestResult.SingleTest -> listOf(it)
            is TestResult.TestSuite -> it.tests
        }
    }

    val failedTests = allTests.filterNot{ it.passed }

    val passCount = allTests.size - failedTests.size

    testResults.forEach { result ->
        if (result.passed) {
            println("${" PASS ".greenBackground()} ${result.name} (${result.runtime}ms)")
        } else {
            println("${" FAIL ".redBackground()} ${result.name} (${result.runtime}ms)")
            if (result is TestResult.TestSuite) {
                result.tests
                    .filterNot { it.passed }
                    .forEach { test ->
                        println("       ${"✗".red()} ${test.name}")
                    }
            }
        }
    }

    if (failedTests.isNotEmpty()) {
        println()
        println("Failures:\n")

        failedTests.forEach { result ->
            println("${" FAIL ".redBackground()} ${result.name} (${result.runtime}ms)")
            val failureLines = "       " + (result.failureMessage ?: "").split("\\n").joinToString("\n       ")
            println(failureLines)
        }
    }

    println()
    println("Runtime: ${testResults.sumBy { it.runtime.toInt() }}ms")
    print("Results: ")

    val summaryText = mutableListOf<String>()
    if (passCount > 0) {
        summaryText += "$passCount passed".green()
    }

    if (failedTests.isNotEmpty()) {
        summaryText += "${failedTests.size} failed".red()
    }

    summaryText += "${passCount + failedTests.size} total"

    println(summaryText.joinToString(", "))

    println()

    if (failedTests.isNotEmpty()) {
        exitProcess(1)
    } else {
        exitProcess(0)
    }
}

private fun File.listFilesRecursively(): List<File> {
    return if (isDirectory) {
        listFiles().map { it.listFilesRecursively() }.flatten()
    } else {
        listOf(this)
    }
}

inline fun <reified T> Any.asA(): T {
    return this as T
}
