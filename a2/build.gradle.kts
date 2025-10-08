plugins {
    id 'java'
    id 'application'
}

group = 'comp512p2'
version = '1.0.0'

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
}

dependencies {
    // The project ships with a local compiled jar that the sources depend on.
    implementation files('comp512p2.jar')
}

sourceSets {
    main {
        java {
            // Keep existing layout under comp512st
            srcDirs = ['comp512st']
        }
    }
}

// Do not set a default mainClass; we'll provide run tasks for the apps below.

tasks.register('runTiApp', JavaExec) {
    group = 'application'
    description = 'Run TreasureIslandApp (interactive)'
    classpath = sourceSets.main.runtimeClasspath
    mainClass.set('comp512st.tiapp.TreasureIslandApp')
}

tasks.register('runTiAppAuto', JavaExec) {
    group = 'application'
    description = 'Run TreasureIslandAppAuto (automated)'
    classpath = sourceSets.main.runtimeClasspath
    mainClass.set('comp512st.tests.TreasureIslandAppAuto')
}

// Make the normal build compile the Java sources
tasks.named('build') {
    dependsOn 'classes'
}
