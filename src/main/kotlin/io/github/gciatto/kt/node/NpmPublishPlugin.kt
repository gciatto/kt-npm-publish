package io.github.gciatto.kt.node

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Exec
import org.jetbrains.kotlin.gradle.targets.js.npm.PublicPackageJsonTask

class NpmPublishPlugin : Plugin<Project> {

    private lateinit var extension: NpmPublishExtension

    private fun Project.createNpmLoginTask(name: String): DefaultTask {
        val setRegistryName = "${name}SetRegistry"
        val setRegistry = rootProject.tasks.maybeCreate(setRegistryName, SetRegistryTask::class.java).also {
            it.defaultValuesFrom(extension)
        }
        val setToken = rootProject.tasks.maybeCreate("${name}SetToken", SetTokenTask::class.java).also {
            it.defaultValuesFrom(extension)
        }
        setToken.dependsOn(setRegistry)
        return rootProject.tasks.maybeCreate(name, DefaultTask::class.java).also {
            it.group = "nodeJs"
            it.dependsOn(setRegistry)
            it.dependsOn(setToken)
        }
    }

    private fun Project.createNpmPublishTask(name: String): Exec {
        return tasks.maybeCreate(name, NpmPublishTask::class.java).also {
            it.defaultValuesFrom(extension)
        }
    }

    private fun Project.createCopyRootProjectFilesTask(name: String): Task {
        return tasks.maybeCreate(name, CopyRootProjectFilesTask::class.java).also {
            it.defaultValuesFrom(extension)
        }
    }

    private fun Project.createLiftJsSourceTask(name: String): DefaultTask {
        return tasks.maybeCreate(name, LiftJsSourcesTask::class.java).also {
            it.defaultValuesFrom(extension)
        }
    }

    private fun Project.createLiftPackageJsonTask(name: String): LiftPackageJsonTask {
        return tasks.maybeCreate(name, LiftPackageJsonTask::class.java).also { liftTask ->
            liftTask.defaultValuesFrom(extension)
            tasks.withType(PublicPackageJsonTask::class.java).matching {
                it.name.contains("test", ignoreCase = true).not()
            }.all {
                liftTask.dependsOn(it)
                extension.log(this) {
                    "Found task ${it.path}: ${liftTask.path} will depend on it"
                }
            }
        }
    }

    override fun apply(target: Project) {
        extension = target.extensions.create(NpmPublishExtension.NAME, NpmPublishExtension::class.java)
        extension.verbose = target.findProperty(VERBOSE_PROPERTY)?.toString().toBoolean()
        extension.defaultValuesFrom(target)
        val login = target.createNpmLoginTask("npmLogin")
        val publish = target.createNpmPublishTask("npmPublish")
        val liftPackageJson = target.createLiftPackageJsonTask("liftPackageJson")
        val copy = target.createCopyRootProjectFilesTask("copyFilesNextToPackageJson")
        val liftJsSourcesTask = target.createLiftJsSourceTask("liftJsSources")
        publish.dependsOn(login)
        publish.dependsOn(liftPackageJson)
        publish.dependsOn(liftJsSourcesTask)
        liftPackageJson.dependsOn(copy)
    }

    companion object {
        const val VERBOSE_PROPERTY: String = "io.github.gciatto.kt-npm-publish.verbose"
    }
}
