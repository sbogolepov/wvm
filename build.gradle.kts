plugins {
    kotlin("multiplatform") version "1.4.30-M1"
}

group = "me.sbogolepov"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = "https://kotlin.bintray.com/kotlinx/")
    jcenter()
}

kotlin {

    jvm()
    macosX64 {
        binaries {
            executable("wvm-run") {
                entryPoint = "main"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.1.1")
                implementation("com.squareup.okio:okio-multiplatform:3.0.0-alpha.1")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }
    }
}

kotlin.sourceSets.all {
    languageSettings.useExperimentalAnnotation("kotlin.ExperimentalStdlibApi")
    languageSettings.useExperimentalAnnotation("kotlin.ExperimentalUnsignedTypes")
    languageSettings.useExperimentalAnnotation("kotlinx.cli.ExperimentalCli")
    languageSettings.useExperimentalAnnotation("okio.ExperimentalFileSystem")
}