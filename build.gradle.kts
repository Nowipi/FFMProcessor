import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("java-library")
    id("signing")
    id("com.vanniktech.maven.publish") version "0.32.0"
}

group = "io.github.nowipi"
version = "1.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

signing {
    useInMemoryPgpKeys(
        project.findProperty("signing.keyId") as String?,
        project.findProperty("signing.secretKey") as String?,
        project.findProperty("signing.password") as String?
    )
}


mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    signAllPublications()

    coordinates("io.github.nowipi", "ffmprocessor", project.version.toString())

    pom {
        name.set("FFM Processor")
        description.set("Generates Java FFM bindings with annotations")
        url.set("https://github.com/Nowipi/FFMProcessor/")
        licenses {
            license {
                name.set("The MIT License (MIT)")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("https://opensource.org/licenses/MIT")
            }
        }
        developers {
            developer {
                id.set("Nowipi")
                name.set("Noah Uyttebroeck")
                url.set("https://github.com/Nowipi/")
            }
        }
        scm {
            url.set("https://github.com/Nowipi/FFMProcessor/")
            connection.set("scm:git:git://github.com/Nowipi/FFMProcessor.git")
            developerConnection.set("scm:git:ssh://git@github.com/Nowipi/FFMProcessor.git")
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testAnnotationProcessor(sourceSets.main.get().output)
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("-Djava.library.path=C:\\Users\\noahu\\IdeaProjects\\FFMProcessor\\src\\test\\resources")
}