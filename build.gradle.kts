@file:Suppress("SpellCheckingInspection")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.serialization") version "1.9.23"
    id("fabric-loom") version "1.6-SNAPSHOT"

    id("com.modrinth.minotaur") version "2.8.7"
    id("com.github.breadmoirai.github-release") version "2.5.2"
    `maven-publish`
    signing
}

group = "dev.nyon"
val majorVersion = "1.1.1"
val mcVersion = "1.20.5-pre1"
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
    maven("https://maven.isxander.dev/snapshots")
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings(
        loom.layered {
            parchment("org.parchmentmc.data:parchment-1.20.4:2024.02.25@zip")
            officialMojangMappings()
        }
    )
    implementation("org.vineflower:vineflower:1.9.3")
    modImplementation("net.fabricmc:fabric-loader:0.15.10")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.96.15+1.20.5")
    modImplementation("net.fabricmc:fabric-language-kotlin:1.10.19+kotlin.1.9.23")
    modImplementation("dev.isxander.yacl:yet-another-config-lib-fabric:3.3.2+1.20.4+update.1.20.5-SNAPSHOT+update.1.20.5-SNAPSHOT")
    modImplementation("com.terraformersmc:modmenu:10.0.0-alpha.3")
    include(modImplementation("dev.nyon:konfig:2.0.1-1.20.4")!!)
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
        options.release.set(21)
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "21"
    }
}
val changelogText =
    buildString {
        append("# v${project.version}\n")
        file("changelog.md").readText().also { append(it) }
    }

modrinth {
    token.set(findProperty("modrinth.token")?.toString())
    projectId.set("KRVzVd0T")
    versionNumber.set(project.version.toString())
    versionName.set(project.version.toString())
    versionType.set("release")
    uploadFile.set(tasks["remapJar"])
    gameVersions.set(listOf("1.20.5"))
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
    owner = split[0]
    repo = split[1]
    releaseName = project.version.toString()
    tagName = project.version.toString()
    body = changelogText
    targetCommitish = "master"
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
