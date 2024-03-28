plugins {
    kotlin("jvm") version "1.7.21"
    kotlin("plugin.serialization") version "1.7.21"
    `maven-publish`
}

group = "ru.vood.json.mutation.lib"
version = "1.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
//    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.4.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
//    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.withType<Jar>() {

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

//    manifest {
//        attributes["Main-Class"] = "MainKt"
//    }

    configurations["compileClasspath"].forEach { file: File ->
        from(zipTree(file.absoluteFile))
    }
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