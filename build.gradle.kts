plugins {
    kotlin("jvm") version "1.7.21"
    `maven-publish`
}

group = "ru.vood.json.mutation.lib"
version = "1.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.4.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

publishing{
    publications{
        create<MavenPublication>("maven") {
            groupId = "ru.vood.json.mutation.lib"
            artifactId = "json-mutation-lib"
            version = "1.1"

            from(components["java"])
        }
    }
}