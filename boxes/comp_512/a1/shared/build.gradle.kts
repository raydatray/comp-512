plugins {
    `java-library`
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

sourceSets {
    getByName("main") {
        java.srcDirs("lib")
    }
}