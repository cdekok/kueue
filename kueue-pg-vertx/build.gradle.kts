plugins {
    alias(libs.plugins.kotlinxSerialization)
}

dependencies {
    implementation(project(":kueue-core"))
    implementation(project(":kueue-retry"))
    implementation(libs.vertxCoroutines)
    implementation(libs.vertxPg)
    implementation(libs.kotlinxCoroutines)
    implementation(libs.kotlinxJson)
    implementation(libs.kotlinLogging)
    implementation(libs.uuidCreator)
}
