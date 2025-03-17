plugins {
    signing
    `java-library`
    `maven-publish`
    id("org.jetbrains.dokka")
}

group = "dev.stephanson"
description = "A library used with swagger codegen to provide older micronaut applications an out of the box solution for dealing with swagger."
version = "0.0.1"

java {
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    implementation("io.swagger:swagger-codegen:2.4.41")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

publishing {
    publications.create<MavenPublication>("local") {
        artifactId = "micronaut3-swagger-generator"
        groupId = project.group.toString()
        version = project.version.toString()

        from(components["java"])
    }
}
