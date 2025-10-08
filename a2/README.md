Gradle build for comp512p2

This project was converted to a Gradle build. The sources live under `comp512st` and the repository includes a prebuilt `comp512p2.jar` which is used as a dependency.

Build
-----

Run from the project root:

```bash
./gradlew build
```

Run
---

Run the interactive TreasureIslandApp:

```bash
./gradlew runTiApp --args='localhost:3000 localhost:3001,localhost:3002 game1 3 0'
```

Run the automated app:

```bash
./gradlew runTiAppAuto --args='localhost:3000 localhost:3001,localhost:3002 game1 3 0 10 100 seed'
```

Notes
-----

- Gradle uses Java 8 compatibility by default here. Adjust `build.gradle` if you need a different Java version.
- The project depends on the included `comp512p2.jar`. If you want to build that jar from source, add the appropriate subproject.
