@file:Suppress("SpellCheckingInspection")
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.nio.file.Path
import kotlin.io.path.notExists
import kotlin.io.path.readText

plugins {
    kotlin("jvm") version "1.9.10"
    kotlin("plugin.serialization") version "1.9.10"
    id("fabric-loom") version "1.4-SNAPSHOT"

    id("com.modrinth.minotaur") version "2.8.4"
    id("com.github.breadmoirai.github-release") version "2.4.1"
    `maven-publish`
    signing
}

group = "dev.nyon"
val majorVersion = "1.0.1"
val mcVersion = "1.20.2"
version = "$majorVersion-$mcVersion"
description = "Fabric/Quilt mod which allows you to use spawners as a fully automatic farm"
val authors = listOf("btwonion")
val githubRepo = "btwonion/compactspawners"

repositories {
    mavenCentral()
    maven("https://maven.terraformersmc.com")
    maven("https://maven.parchmentmc.org")
    maven("https://maven.isxander.dev/releases")
    maven("https://repo.nyon.dev/releases")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings(loom.layered {
        parchment("org.parchmentmc.data:parchment-1.20.2:2023.10.22@zip")
        officialMojangMappings()
    })
    modImplementation("net.fabricmc:fabric-loader:0.14.24")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.90.4+$mcVersion")
    modImplementation("net.fabricmc:fabric-language-kotlin:1.10.10+kotlin.1.9.10")
    modImplementation("dev.isxander.yacl:yet-another-config-lib-fabric:3.2.1+$mcVersion")
    include(implementation(annotationProcessor("io.github.llamalad7:mixinextras-fabric:0.2.0-rc.5")!!)!!)
    modApi("com.terraformersmc:modmenu:8.0.0")
    include(modImplementation("dev.nyon:konfig:1.0.4-$mcVersion")!!)
}

tasks {
    processResources {
        val modId = "compactspawners"
        val modDescription = "Fabric/Quilt mod which allows you to use spawners as a fully automatic farm"

        inputs.property("id", modId)
        inputs.property("name", modId)
        inputs.property("description", modDescription)
        inputs.property("version", project.version)

        filesMatching("fabric.mod.json") {
            expand(
                mutableMapOf(
                    "id" to modId,
                    "name" to modId,
                    "description" to modDescription,
                    "version" to project.version.toString()
                )
            )
        }
    }

    register("releaseMod") {
        group = "publishing"

        dependsOn("modrinthSyncBody")
        dependsOn("modrinth")
        dependsOn("githubRelease")
        dependsOn("publish")
    }

    withType<JavaCompile> {
        options.release.set(17)
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }
}
val changelogFile: Path = rootDir.toPath().resolve("changelogs/$version.md")
val changelogText = if (changelogFile.notExists()) "" else changelogFile.readText()

modrinth {
    token.set(findProperty("modrinth.token")?.toString())
    projectId.set("KRVzVd0T")
    versionNumber.set(project.version.toString())
    versionName.set(project.version.toString())
    versionType.set("release")
    uploadFile.set(tasks["remapJar"])
    gameVersions.set(listOf("1.20.2"))
    loaders.set(listOf("fabric", "quilt"))
    dependencies {
        required.project("fabric-api")
        required.project("fabric-language-kotlin")
        required.project("yacl")
        required.project("modmenu")
    }
    changelog.set(changelogText)
    syncBodyFrom.set(file("README.md").readText())
}

githubRelease {
    token(findProperty("github.token")?.toString())

    val split = githubRepo.split("/")
    owner(split[0])
    repo(split[1])
    releaseName(project.version.toString())
    tagName(project.version.toString())
    body(changelogText)
    targetCommitish("master")
    setReleaseAssets(tasks["remapJar"])
}

publishing {
    repositories {
        maven {
            name = "nyon"
            url = uri("https://repo.nyon.dev/releases")
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = "dev.nyon"
            artifactId = "compactspawners"
            version = project.version.toString()
            from(components["java"])
        }
    }
}

java {
    withSourcesJar()
}

signing {
    sign(publishing.publications)
}