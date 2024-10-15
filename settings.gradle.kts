plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "DotenvGenerator"
include("dotenv-generator-ksp")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include("dotenv-generator-anno")
