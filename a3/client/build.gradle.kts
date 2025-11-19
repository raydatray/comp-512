plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    implementation(project(":task"))

    implementation("org.apache.zookeeper:zookeeper:3.8.5")
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

sourceSets {
    getByName("main") {
        java.srcDirs("src/main/java")
    }

    getByName("test") {
        java.srcDirs("src/test/java")
    }
}

application {
    mainClass = "DistClient"
}
