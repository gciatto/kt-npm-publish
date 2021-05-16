package io.github.gciatto.kt.node

import com.google.gson.JsonObject
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.TaskCollection
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsSetupTask
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinPackageJsonTask
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import java.io.File

open class NpmPublishExtension(objects: ObjectFactory, private val providers: ProviderFactory) {

    companion object {

        const val NAME = "npmPublishing"

        private val isWindows: Boolean
            get() = File.separatorChar == '\\'

        private const val npmScriptSubpath = "node_modules/npm/bin/npm-cli.js"

        private val possibleNodePaths: Sequence<String> =
            sequenceOf("bin/node", "node").let { paths ->
                if (isWindows) {
                    paths.map { "$it.exe" } + paths
                } else {
                    paths
                }
            }

        private val possibleNpmPaths: Sequence<String> =
            sequenceOf("lib/", "").map { it + npmScriptSubpath }
    }

    var verbose = false

    private val nodeRoot: Property<File> = objects.property()

    val nodeSetupTask: Property<String> = objects.property()

    val jsCompileTask: Property<String> = objects.property()

    val packageJson: Property<File> = objects.property()

    val token: Property<String> = objects.property()

    val registry: Property<String> = objects.property(String::class.java).also {
        it.set("registry.npmjs.org")
    }

    val node: Provider<File> = nodeRoot.map { nodeRoot ->
        possibleNodePaths.map { nodeRoot.resolve(it) }.first { it.exists() }
    }

    val npm: Provider<File> = nodeRoot.map { nodeRoot ->
        possibleNpmPaths.map { nodeRoot.resolve(it) }.first { it.exists() }
    }

    val npmProject: Provider<File> = packageJson.map { it.parentFile }

    val jsSourcesDir: Property<File> = objects.property(File::class.java).also {
        File("build/")
    }

    internal val packageJsonLiftingActions = objects.listProperty<Action<PackageJson>>()

    internal val packageJsonRawLiftingActions = objects.listProperty<Action<JsonObject>>()

    internal val jsSourcesLiftingActions = objects.listProperty<FileLineTransformer>()

    fun liftPackageJson(action: Action<PackageJson>) {
        with(packageJsonLiftingActions) {
            add(action)
        }
    }

    fun liftPackageJsonRaw(action: Action<JsonObject>) {
        with(packageJsonRawLiftingActions) {
            add(action)
        }
    }

    fun liftJsSources(lineTransformer: FileLineTransformer) {
        with(jsSourcesLiftingActions) {
            add(lineTransformer)
        }
    }

    internal fun warn(project: Project, message: () -> String) {
        if (verbose) {
            println("[WARNING] [${project.name}] [$NAME]: ${message()}")
        }
    }

    internal fun log(project: Project, message: () -> String) {
        if (verbose) {
            println("[${project.name}] [$NAME]: ${message()}")
        }
    }

    private val Project.nodeJsSetupTasks: TaskCollection<NodeJsSetupTask>
        get() = rootProject.tasks.withType()

    private fun defaultNodeRootValueFrom(project: Project) {
        project.nodeJsSetupTasks.all { nodeJsSetupTask ->
            nodeRoot.set(
                providers.provider {
                    nodeJsSetupTask.destination.also {
                        log(project) {
                            "Inferred ${NpmPublishExtension::nodeRoot.name} from task ${nodeJsSetupTask.path}: $it"
                        }
                    }
                }
            )
        }
    }

    private fun defaultNodeSetupTaskValueFrom(project: Project) {
        project.nodeJsSetupTasks.all { nodeJsSetupTask ->
            nodeSetupTask.set(
                providers.provider {
                    nodeJsSetupTask.path.also {
                        log(project) {
                            "Inferred ${NpmPublishExtension::nodeSetupTask.name}: ${nodeJsSetupTask.path}"
                        }
                    }
                }
            )
        }
    }

    private fun defaultPackageJsonValueFrom(project: Project) {
        project.tasks.withType<KotlinPackageJsonTask>()
            .matching { !it.name.contains("test", ignoreCase = true) }
            .all { packageJsonTask ->
                packageJson.set(
                    providers.provider {
                        try {
                            packageJsonTask.packageJson.parentFile.resolve("package.json").also {
                                log(project) {
                                    "Inferred ${NpmPublishExtension::packageJson.name} " +
                                        "from task ${packageJsonTask.path}: $it"
                                }
                            }
                        } catch (_: UninitializedPropertyAccessException) {
                            warn(project) {
                                "Cannot infer ${NpmPublishExtension::packageJson.name} " +
                                    "from task ${packageJsonTask.path}"
                            }
                            null
                        }
                    }
                )
            }
    }

    private val Project.mainKotlin2JsCompileTasks: TaskCollection<Kotlin2JsCompile>
        get() = tasks.withType<Kotlin2JsCompile>().matching { !it.name.contains("test", ignoreCase = true) }

    private fun defaultJsCompileTaskValueFrom(project: Project) {
        project.mainKotlin2JsCompileTasks.all { kt2JsCompileTask ->
            jsCompileTask.set(
                providers.provider {
                    kt2JsCompileTask.path.also {
                        log(project) { "Inferred ${NpmPublishExtension::jsCompileTask.name}: $it" }
                    }
                }
            )
        }
    }

    private fun defaultJsSourcesDirValueFrom(project: Project) {
        project.mainKotlin2JsCompileTasks.all { kt2JsCompileTask ->
            jsSourcesDir.set(
                providers.provider {
                    kt2JsCompileTask.outputFile.parentFile.also {
                        log(project) {
                            "Inferred ${NpmPublishExtension::jsSourcesDir.name} " +
                                "from task ${kt2JsCompileTask.path}: ${kt2JsCompileTask.outputFile.parentFile}"
                        }
                    }
                }
            )
        }
    }

    fun defaultValuesFrom(project: Project) {
        defaultNodeRootValueFrom(project)
        defaultNodeSetupTaskValueFrom(project)
        defaultPackageJsonValueFrom(project)
        defaultJsCompileTaskValueFrom(project)
        defaultJsSourcesDirValueFrom(project)
    }
}
