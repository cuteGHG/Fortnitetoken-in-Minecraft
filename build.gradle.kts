plugins {
    java
}

group = "de.cuteghg"
version = "1.1.0"

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.3.build.+")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(25)
}

tasks.processResources {
    filteringCharset = "UTF-8"
}

// Reproducible JAR output: stable file order and no build-time timestamps.
// This improves auditability; it does not bypass or disable antivirus detection.
tasks.withType<Jar>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
    manifest {
        attributes(
            "Implementation-Title" to "FortniteToken",
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "cuteGHG"
        )
    }
}
