package org.danilopianini.template

import java.io.Serializable
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.register

open class HelloGradle : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create<HelloExtension>("hello")
        target.tasks.register<HelloTask>("hello") {
            author.set(extension.author)
        }
    }
}

open class HelloTask : DefaultTask() {
    @Input
    val author: Property<String> = project.objects.property()

    // Read-only property calculated from the greeting
    @Internal
    val message: Provider<String> = author.map { "Hello from $it" }

    @TaskAction
    fun printMessage() {
        logger.quiet(message.get())
    }
}

open class HelloExtension(objects: ObjectFactory) : Serializable {
    val author: Property<String> = objects.property()
}
