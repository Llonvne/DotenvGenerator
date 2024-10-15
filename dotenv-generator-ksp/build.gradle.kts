plugins {
    kotlin("jvm")
}

group = "cn.llonvne"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:2.0.21-1.0.25")
    implementation("com.squareup:kotlinpoet-ksp:1.18.1")
    implementation(projects.dotenvGeneratorAnno)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}