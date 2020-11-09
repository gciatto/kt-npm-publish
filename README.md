# Publishing Kotlin Multi-Platform Projects to NPM

A Gradle plugin letting developers upload Kotlin-JS or -MPP projects on NPM.
It requires a Gradle project including one of the following plugins:
- `org.jetbrains.kotlin.js`
- `org.jetbrains.kotlin.multiplatform`

In both cases, the plugin assumes a Node Js target has been added to your project, explicitly, via the syntax:
```kotlin
kotlin {
    js {
        nodeJs {
            // ...
        }
    }
}
```

The plugin does _not_ apply any of the aforementioned `org.jetbrains.kotlin.*` plugins behind the scenes.
Thus, it is important to apply them accordingly.

