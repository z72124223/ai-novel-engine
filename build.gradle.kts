import org.gradle.api.tasks.SourceSetContainer

plugins {
    kotlin("jvm") version "2.0.21" apply false
    kotlin("plugin.serialization") version "2.0.21" apply false
}

tasks.register<JavaExec>("run") {
    group = "application"
    description = "Runs the CLI from the app module."
    dependsOn(":app:classes")
    mainClass.set("com.localnovel.engine.MainKt")
    classpath = project(":app").extensions.getByType<SourceSetContainer>()["main"].runtimeClasspath
    workingDir = rootProject.projectDir
}
