plugins {
    java
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // none
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
