plugins {
    signing
    `java-library`
    `maven-publish`
    id("org.jetbrains.dokka")
    id("io.micronaut.library") version "3.7.10"
    id("org.openapi.generator") version "7.7.0"
}

group = "dev.stephanson"
version = "0.0.1"

java {
    withSourcesJar()
    withJavadocJar()
}

micronaut {
    version = "3.9.0"
}

dependencies {

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

publishing {
    publications.create<MavenPublication>("local") {
        artifactId = "micronaut3-openapi-generator"
        groupId = project.group.toString()
        version = project.version.toString()

        from(components["java"])
    }
}
