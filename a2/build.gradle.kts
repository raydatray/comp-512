plugins {
    application
}

group = "comp512p2"
version = "1.0.0"

java {
    // Use toolchain or compatibility for Java 8
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
}

dependencies {
    // The project ships with a local compiled jar that the sources depend on.
    implementation(files("comp512p2.jar"))
}

sourceSets {
    named("main") {
        java {
            // Keep existing layout under comp512st
            setSrcDirs(listOf("comp512st"))
        }
    }
}

// Do not set a default mainClass; we'll provide run tasks for the apps below.

tasks.register<JavaExec>("runTiApp") {
    group = "application"
    description = "Run TreasureIslandApp (interactive)"
    classpath = sourceSets.named("main").get().runtimeClasspath
    mainClass.set("tiapp.TreasureIslandApp")
}

tasks.register<JavaExec>("runTiAppAuto") {
    group = "application"
    description = "Run TreasureIslandAppAuto (automated)"
    classpath = sourceSets.named("main").get().runtimeClasspath
    mainClass.set("tests.TreasureIslandAppAuto")
}

// Make the normal build compile the Java sources
tasks.named("build") {
     dependsOn("classes")
}