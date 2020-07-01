package io.github.gciatto.kt.node

import java.io.File

typealias FileLineTransformer = (File, Int, String) -> String