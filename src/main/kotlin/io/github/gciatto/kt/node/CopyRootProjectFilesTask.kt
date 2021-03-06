package io.github.gciatto.kt.node

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.property
import java.io.File

open class CopyRootProjectFilesTask : Copy() {

    @Input
    protected val jsCompileTask: Property<String> = project.objects.property()

    @Input
    protected val packageJson: Property<File> = project.objects.property()

    open fun defaultValuesFrom(extension: NpmPublishExtension) {
        jsCompileTask.set(extension.jsCompileTask)
        packageJson.set(extension.packageJson)
    }

    init {
        group = "nodeJs"
        dependsOn(jsCompileTask)
        inputs.files("README*", "CONTRIB*", "LICENSE*")
        outputs.files(packageJson)
        from(project.rootProject.projectDir)
        include("README*")
        include("CONTRIB*")
        include("LICENSE*")
        into(packageJson.map { it.parentFile })
    }
}
