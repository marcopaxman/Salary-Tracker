plugins {
    // Versions aligned with AGP 8.5.x and Compose Compiler 1.5.15 (requires Kotlin 1.9.25)
    id("com.android.application") version "8.5.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.25" apply false
    id("com.google.devtools.ksp") version "1.9.25-1.0.20" apply false
}

// Convenience clean task (use Gradle types without applying repository config here)
tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
