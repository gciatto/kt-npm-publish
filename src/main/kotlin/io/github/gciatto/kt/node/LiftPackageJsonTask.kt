package io.github.gciatto.kt.node

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import org.gradle.api.Action
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property
import java.io.File
import java.io.FileReader
import java.io.FileWriter

open class LiftPackageJsonTask : AbstractNodeDefaultTask() {

    private val gson = GsonBuilder().setPrettyPrinting().create()

    @Input
    protected val packageJsonFile: Property<File> = project.objects.property()

    private var packageJson: PackageJson? = null

    private lateinit var packageJsonRaw: JsonObject

    private val liftingActions = project.objects.listProperty<Action<PackageJson>>()

    private val rawLiftingActions = project.objects.listProperty<Action<JsonObject>>()

    override fun defaultValuesFrom(extension: NpmPublishExtension) {
        super.defaultValuesFrom(extension)
        packageJsonFile.set(extension.packageJson)
        liftingActions.set(extension.packageJsonLiftingActions)
        rawLiftingActions.set(extension.packageJsonRawLiftingActions)
    }

    private lateinit var actualPackageJsonFile: File

    @TaskAction
    fun lift() {
        actualPackageJsonFile = packageJsonFile.get()
        if (!actualPackageJsonFile.exists()) {
            actualPackageJsonFile = actualPackageJsonFile.parentFile.resolve("pre-package.json")
        }
        if (!actualPackageJsonFile.exists()) {
            error("File ${packageJsonFile.get().path} does not exist")
        }
        resolve()
        performLifting()
        save()
    }

    private fun resolve() {
        packageJsonRaw = gson.fromJson(FileReader(actualPackageJsonFile), JsonObject::class.java)
        packageJson = try {
            PackageJson.fromJson(packageJsonRaw)
        } catch (_: Throwable) {
            System.err.println("Cannot parse $actualPackageJsonFile as a data class, use raw lifting")
            null
        }
    }

    private fun performLifting() {
        if (packageJson == null) {
            rawLiftingActions.getOrElse(emptyList()).forEach { it.execute(packageJsonRaw) }
        } else {
            liftingActions.getOrElse(emptyList()).forEach { it.execute(packageJson!!) }
        }
    }

    private fun save() {
        FileWriter(actualPackageJsonFile.parentFile.resolve("package.json")).use {
            gson.toJson(packageJson?.toJson() ?: packageJsonRaw, it)
        }
    }
}
