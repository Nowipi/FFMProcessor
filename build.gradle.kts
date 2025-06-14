plugins {
    id("java-library")
}

group = "io.github.nowipi"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testAnnotationProcessor(sourceSets.main.get().output)
}

tasks.test {
    useJUnitPlatform()
}