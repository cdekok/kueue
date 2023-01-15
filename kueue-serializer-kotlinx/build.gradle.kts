plugins {
    alias(libs.plugins.kotlinxSerialization)
}

dependencies {
    implementation(project(":kueue-core"))
    implementation(libs.kotlinxJson)
    implementation(libs.kotlinxCoroutines)
}
