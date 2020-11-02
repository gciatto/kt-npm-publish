
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    jacoco
    `java-gradle-plugin`
    kotlin("jvm")
    `maven-publish`
    signing
    id("com.gradle.plugin-publish")
    id("io.gitlab.arturbosch.detekt")
    id("org.jetbrains.dokka")
    id("org.jlleitschuh.gradle.ktlint")
    id("org.danilopianini.git-sensitive-semantic-versioning")
    id("org.danilopianini.publish-on-central")
}

/*
 * Project information
 */
group = "io.github.gciatto"
description = "A plugin easing the publishin of Kotlin JS/Multiplaftorm projects on NPM"
inner class NpmPublishInfo {
    val longName = "Publish Kotlin projects on NPM"
    val website = "https://github.com/gciatto/kt-npm-publish"
    val scm = "git@github.com:gciatto/kt-npm-publish.git"
    val pluginImplementationClass = "$group.kt.node.NpmPublishPlugin"
    val tags = listOf("kotlin", "multi plaftorm", "js", "javascript", "publish", "npm", "gradle")
    val license = "Apache 2.0"
    val licenseUrl = "https://www.apache.org/licenses/LICENSE-2.0"
}
val info = NpmPublishInfo()

gitSemVer {
    version = computeGitSemVer()
}

println("$group:$name v$version")

repositories {
    mavenCentral()
    jcenter()
    mapOf(
        "kotlin/dokka" to setOf("org.jetbrains.dokka"),
        "kotlin/kotlinx.html" to setOf("org.jetbrains.kotlinx"),
        "arturbosch/code-analysis" to setOf("io.gitlab.arturbosch.detekt")
    ).forEach { (uriPart, groups) ->
        maven {
            url = uri("https://dl.bintray.com/$uriPart")
            content { groups.forEach { includeGroup(it) } }
        }
    }
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:_")
    api(gradleApi())
    api(gradleKotlinDsl())
    api("org.jetbrains.kotlin:kotlin-gradle-plugin:_")
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.google.code.gson", "gson", "_")
    testImplementation(gradleTestKit())
    testImplementation("com.uchuhimo:konf-yaml:_")
    testImplementation("io.github.classgraph:classgraph:_")
    testImplementation("io.kotest:kotest-runner-junit5:_")
    testImplementation("io.kotest:kotest-assertions-core-jvm:_")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:_")
    testImplementation("org.mockito:mockito-core:_")
//    testRuntimeOnly(files(createClasspathManifest))
}

kotlin {
    target {
        compilations.all {
            kotlinOptions {
                allWarningsAsErrors = true
//                freeCompilerArgs = listOf("-XXLanguage:+InlineClasses", "-Xopt-in=kotlin.RequiresOptIn")
                jvmTarget = JavaVersion.VERSION_1_8.toString()
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
        showCauses = true
        showStackTraces = true
        events(*TestLogEvent.values())
        exceptionFormat = TestExceptionFormat.FULL
    }
}

tasks.jacocoTestReport {
    reports {
        // Used by Codecov.io
        xml.isEnabled = true
    }
}

detekt {
    failFast = true
    buildUponDefaultConfig = true
    config = files("$projectDir/config/detekt.yml")
    reports {
        html.enabled = true
        xml.enabled = true
        txt.enabled = true
    }
}

pluginBundle {
    website = info.website
    vcsUrl = info.website
    tags = info.tags
}

gradlePlugin {
    plugins {
        create("NpmPublish") {
            id = "${rootProject.group}.${rootProject.name}"
            displayName = info.longName
            description = project.description
            implementationClass = info.pluginImplementationClass
        }
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
}

publishing {
    publications {
        withType<MavenPublication> {
            pom {
                name.set(info.longName)
                description.set(project.description)
                packaging = "jar"
                url.set(info.website)
                if (!this@withType.name.contains("MavenCentral", ignoreCase=true)) {
                    licenses {
                        license {
                            name.set(info.license)
                            url.set(info.licenseUrl)
                        }
                    }
                }
                scm {
                    url.set(info.website)
                    connection.set(info.scm)
                    developerConnection.set(info.scm)
                }
                developers {
                    developer {
                        name.set("Giovanni Ciatto")
                        email.set("giovanni.ciatto@gmail.com")
                        url.set("https://about.me/gciatto")
                    }
                }
            }
        }
    }
}

/*
 * Publication on Maven Central and the Plugin portal
 */
publishOnCentral {
    projectLongName.set(info.longName)
    projectDescription.set(description)
    projectUrl.set(info.website)
    scmConnection.set(info.scm)
    licenseName.set(info.license)
    licenseUrl.set(info.licenseUrl)
}
