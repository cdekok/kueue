plugins {
    alias(libs.plugins.kotlinxSerialization)
}

dependencies {
    implementation(project(":kueue-core"))
    implementation(libs.xstream)
    testImplementation(libs.kotlinxCoroutines)
}
