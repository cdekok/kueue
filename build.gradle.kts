import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import io.gitlab.arturbosch.detekt.Detekt
import java.util.*


plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.detekt)
    alias(libs.plugins.versions)
    alias(libs.plugins.versionCatalogUpdate)
    alias(libs.plugins.kotlinxSerialization)
    `maven-publish`
}

allprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "maven-publish")

    group = "eu.kueue"
    version = rootProject.libs.versions.kueue.get()

    repositories {
        mavenCentral()
    }

    kotlin {
        jvmToolchain(21)
    }

    dependencies {
        val detekt = rootProject.libs.versions.detekt.get()
        detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:$detekt")
        testImplementation(kotlin("test"))
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
        dependencies {
            testRuntimeOnly("org.junit.platform:junit-platform-launcher")
        }
        useJUnitPlatform()
    }

    val publish = listOf(
        "kueue-core",
        "kueue-pg-vertx",
        "kueue-retry",
        "kueue-serializer-kotlinx",
        "kueue-serializer-xstream",
    )

    // Only configure publishing for projects with the Kotlin JVM plugin
    if (publish.contains(name)) {
        publishing {
            publications {
                create<MavenPublication>("maven") {
                    groupId = project.group.toString()
                    artifactId = name
                    version = project.version.toString()

                    // Publish the JAR (and dependencies) from the java component
                    from(components["java"])
                }
            }
        }
    }
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(candidate.version)
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase(Locale.getDefault()).contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

versionCatalogUpdate {
    keep {
        versions.add("kueue")
    }
}
