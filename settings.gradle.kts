pluginManagement {
    plugins {
        id("org.jetbrains.dokka") version "1.9.20" apply false
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }

    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
}


include(
    "micronaut3-swagger",
//    "micronaut3-openapi",
//    "micronaut4-swagger"
)

rootProject.name = "micronaut-codegen"
