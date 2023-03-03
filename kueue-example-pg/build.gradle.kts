plugins {
    alias(libs.plugins.kotlinxSerialization)
    id("application")
}

dependencies {
    implementation(project(":kueue-core"))
    implementation(project(":kueue-pg-vertx"))
    implementation(project(":kueue-serializer-kotlinx"))
    implementation(project(":kueue-serializer-xstream"))
    implementation(libs.kotlinxCoroutines)
    implementation(libs.kotlinxJson)
    implementation(libs.vertxPg)
    implementation(libs.clikt)
    implementation(libs.scram)
    implementation(libs.kotlinLogging)
    implementation(libs.kotlinReflect)
    implementation(libs.logback)
    implementation(libs.xstream)
    testImplementation(libs.testContainers)
    testImplementation(libs.testContainersJunit)
    testImplementation(libs.testContainersPostgresql)
}

application {
    // Define the main class for the application.
    mainClass.set("eu.kueue.example.pg.MainKt")
}
