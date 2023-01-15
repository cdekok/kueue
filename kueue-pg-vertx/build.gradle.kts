plugins {
    alias(libs.plugins.kotlinxSerialization)
}

dependencies {
    implementation(project(":kueue-core"))
    implementation(libs.vertxCoroutines)
    implementation(libs.vertxPg)
    implementation(libs.kotlinxCoroutines)
    implementation(libs.kotlinxJson)
    implementation(libs.kotlinLogging)
}
