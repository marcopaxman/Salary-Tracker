plugins {
    // Versions aligned with Gradle 9.x
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "1.9.25" apply false
    id("com.google.devtools.ksp") version "1.9.25-1.0.20" apply false
    id("com.google.gms.google-services") version "4.4.0" apply false
}

// Convenience clean task (use Gradle types without applying repository config here)
tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
