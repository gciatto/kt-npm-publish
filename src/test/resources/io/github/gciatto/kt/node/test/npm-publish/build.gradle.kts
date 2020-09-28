import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsSetupTask
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinPackageJsonTask
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    id("org.jetbrains.kotlin.js") version "1.4.10"
    id("io.github.gciatto.kt.npm.publish")
}

group = "org.example"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-js"))
}

kotlin {
    js {
        nodejs { }
        binaries.executable()
    }
}

npmPublishing {
//    nodeRoot = rootProject.tasks.withType<NodeJsSetupTask>().asSequence().map { it.destination }.first()
    token = "tokenHere"
//    packageJson = tasks.getByName<KotlinPackageJsonTask>("jsPackageJson").packageJson
//    nodeSetupTask = rootProject.tasks.getByName("kotlinNodeJsSetup").path
//    jsCompileTask = "jsMainClasses"
//    jsSourcesDir = tasks.withType<Kotlin2JsCompile>().asSequence()
//            .filter { "Test" !in it.name }
//            .map { it.outputFile.parentFile }
//            .first()

    liftPackageJson {
        version = "2.3.4"
    }
}