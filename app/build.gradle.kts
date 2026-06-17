plugins {
    application
    kotlin("jvm")
    kotlin("plugin.serialization")
}

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
    doNotTrackState("OneDrive may expose copied resources as non-regular reparse files on this workspace.")
}

tasks.named<JavaExec>("run") {
    workingDir = rootProject.projectDir
}
