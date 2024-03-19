import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
    id("jps-compatible")
}

group = "org.jetbrains.kotlin.experimental.compose"
description = "Contains the Kotlin compiler plugin for Compose used in Android Studio and IDEA"

dependencies {
    implementation(project(":kotlin-stdlib"))
    implementation(project(":js:js.frontend"))
    implementation(project(":compiler:frontend"))
    implementation(project(":compiler:backend.jvm"))
    implementation(project(":compiler:cli-base"))
    implementation(project(":compiler:ir.serialization.js"))
    implementation(project(":compiler:backend.jvm.codegen"))
    implementation(project(":compiler:fir:checkers"))
    implementation(project(":compiler:fir:entrypoint"))
    implementation(project(":compiler:fir:tree"))

    compileOnly(intellijCore())

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit4)
    testRuntimeOnly(libs.junit.vintage.engine)
}

optInToUnsafeDuringIrConstructionAPI()
optInToObsoleteDescriptorBasedAPI()

kotlin.jvmToolchain(8)

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
        freeCompilerArgs.addAll(
            "-Xskip-metadata-version-check",
            "-Xjvm-default=all"
        )
        allWarningsAsErrors.set(false)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

publish {
    pom {
        name.set("AndroidX Compose Hosted Compiler Plugin")
        developers {
            developer {
                name.set("The Android Open Source Project")
            }
        }
    }
}

standardPublicJars()
