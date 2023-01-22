dependencies {
    implementation(project(":kueue-core"))
    implementation(libs.kotlinxCoroutines)
    implementation(libs.kotlinLogging)
    testImplementation(libs.kotlinxCoroutinesTest)
}
