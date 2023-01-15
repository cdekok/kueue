import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import io.gitlab.arturbosch.detekt.Detekt


plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.detekt)
    alias(libs.plugins.versions)
}

allprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    group = "eu.kueue"
    version = rootProject.libs.versions.kueue.get()

    repositories {
        mavenCentral()
    }

    kotlin {
        jvmToolchain(18)
    }

    dependencies {
        val detekt = rootProject.libs.versions.detekt.get()
        detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:$detekt")
        implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
        testImplementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        testImplementation(rootProject.libs.junit)
    }

    tasks.withType<Detekt>().configureEach {
        buildUponDefaultConfig = false
        config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
        include("**/*.kt")
        include("**/*.kts")
        exclude("resources/")
        exclude("build/")
        reports {
            xml.required.set(false)
            html.required.set(false)
            txt.required.set(false)
            sarif.required.set(false)
            md.required.set(false)
        }
    }

    tasks.register<Detekt>("format") {
        description = "Auto format code with detekt."
        autoCorrect = true
        source = files(projectDir).asFileTree
    }

    tasks.test {
        useJUnitPlatform()
    }
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(candidate.version)
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}
