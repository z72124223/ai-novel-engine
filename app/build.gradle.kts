import java.io.File

plugins {
    application
    kotlin("jvm")
    kotlin("plugin.serialization")
}

val externalBuildRoot = providers.environmentVariable("LOCALAPPDATA")
    .orElse(providers.systemProperty("java.io.tmpdir"))

layout.buildDirectory.set(
    layout.dir(externalBuildRoot.map { File(it).resolve("local-interactive-fiction-engine-build/app") }),
)

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("org.xerial:sqlite-jdbc:3.46.1.3")

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("com.localnovel.engine.MainKt")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<ProcessResources>().configureEach {
    doNotTrackState("OneDrive may expose copied resources as non-regular reparse files on this workspace.")
}

tasks.withType<Jar>().configureEach {
    enabled = false
    doNotTrackState("OneDrive may expose copied resources as non-regular reparse files on this workspace.")
}

tasks.named<JavaExec>("run") {
    workingDir = rootProject.projectDir
    onlyIf {
        gradle.startParameter.taskNames.none { it == "run" || it == ":run" }
    }
}
