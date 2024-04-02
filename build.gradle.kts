plugins {
    jacoco
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
    val kotestVersion = "5.6.2"
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
    implementation("io.arrow-kt:arrow-core:1.2.0")

//    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.4.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-framework-datatest:$kotestVersion")
//    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

//tasks.getByName<Test>("test") {
//    useJUnitPlatform()
//}
tasks.withType<Test>().configureEach {
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