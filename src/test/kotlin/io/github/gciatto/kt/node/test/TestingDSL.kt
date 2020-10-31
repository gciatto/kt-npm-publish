package io.github.gciatto.kt.node.test

import com.uchuhimo.konf.ConfigSpec
import java.io.File

object Root : ConfigSpec("") {
    val tests by required<List<Test>>()
}

data class Test(
    val description: String,
    val configuration: Configuration,
    val expectation: Expectation
)

data class Configuration(val tasks: List<String>, val options: List<String> = emptyList())

data class Expectation(
    val file_exists: List<ExistingFile> = emptyList(),
    val success: List<String> = emptyList(),
    val failure: List<String> = emptyList(),
    val output_contains: List<String> = emptyList(),
    val output_matches: List<String> = emptyList()
)

data class ExistingFile(val name: String, val contents: List<String> = emptyList(), val all: Boolean = false) {
    private val regexes by lazy { contents.map { Regex(it) } }
    private fun Sequence<Boolean>.matches(): Boolean = if (all) all { it } else any { it }
    fun isValid(root: File): Boolean {
        val actualFile = actualFile(root)
        val lines = actualFile.readLines()
        return regexes.asSequence().map { regex -> lines.any { regex.containsMatchIn(it) } }.matches()
    }
    val file: File get() = File(name)
    fun actualFile(root: File) = file.let { if (it.isAbsolute) it else root.resolve(it) }
}
