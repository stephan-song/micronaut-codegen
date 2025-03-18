import com.vanniktech.maven.publish.SonatypeHost

plugins {
    `java-library`
    id("com.vanniktech.maven.publish") version "0.31.0-rc2"
}

group = "dev.stephanson"
description =
    "A library used with swagger codegen to provide older micronaut applications an out of the box solution for dealing with swagger."
version = "0.0.1"

dependencies {
    implementation("io.swagger:swagger-codegen:2.4.41")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

mavenPublishing {
    coordinates(project.group.toString(), "micronaut3-swagger-generator", project.version.toString())

    pom {
        name.set("micronaut3-swagger-generator")
        description.set(project.description)
        url.set("https://github.com/stephan-song/micronaut-codegen")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set(providers.environmentVariable("DEVELOPER_ID"))
                name.set(providers.environmentVariable("DEVELOPER_NAME"))
                email.set(providers.environmentVariable("DEVELOPER_EMAIL"))
            }
        }
        scm {
            url.set("https://github.com/stephan-song/micronaut-codegen")
            connection.set("scm:git://github.com:stephan-song/micronaut-codegen.git")
            developerConnection.set("scm:git://github.com:stephan-song/micronaut-codegen.git")
        }
    }

    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = false)
    signAllPublications()
}
