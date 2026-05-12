import top.mrxiaom.gradle.LibraryHelper

plugins {
    java
    `maven-publish`
    id ("com.gradleup.shadow") version "9.3.0"
    id ("com.github.gmazzo.buildconfig") version "5.6.7"
}

buildscript {
    repositories.mavenCentral()
    dependencies.classpath("top.mrxiaom:LibrariesResolver-Gradle:1.7.20")
}
val base = LibraryHelper(project)

group = "top.mrxiaom.sweet.locks"
version = "1.0.6"
val targetJavaVersion = 8
val pluginBaseModules = base.modules.run{ listOf(library, paper, actions, gui, l10n, misc) }
val shadowGroup = "top.mrxiaom.sweet.locks.libs"

repositories {
    mavenCentral()
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.helpch.at/releases/")
    maven("https://jitpack.io")
    maven("https://repo.rosewooddev.io/repository/public/")
}

configurations.create("shadowLink")
dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20.4-R0.1-SNAPSHOT")
    compileOnly(base.depend.annotations)

    compileOnly("net.milkbowl.vault:VaultAPI:1.7")
    compileOnly("me.clip:placeholderapi:2.12.2")

    base.library(LibraryHelper.adventure("4.22.0"))
    base.library(LibraryHelper.adventure("4.4.0", listOf("platform-bukkit")))

    implementation(base.depend.nbtapi)
    implementation("com.github.technicallycoded:FoliaLib:0.4.4") { isTransitive = false }
    for (artifact in pluginBaseModules) {
        implementation(artifact)
    }
    implementation(base.resolver.lite)
    for (nms in project.project(":nms").subprojects) {
        if (nms.name == "shared") implementation(nms)
        else add("shadowLink", nms)
    }
}

LibraryHelper.initJava(project, base, targetJavaVersion, true)
LibraryHelper.initPublishing(project)

buildConfig {
    className("BuildConstants")
    packageName("top.mrxiaom.sweet.locks")

    base.doResolveLibraries()
    buildConfigField("String", "VERSION", "\"${project.version}\"")
    buildConfigField("java.time.Instant", "BUILD_TIME", "java.time.Instant.ofEpochSecond(${System.currentTimeMillis() / 1000L}L)")
    buildConfigField("String[]", "RESOLVED_LIBRARIES", base.join())
}
tasks {
    shadowJar {
        configurations.add(project.configurations.runtimeClasspath.get())
        configurations.add(project.configurations.getByName("shadowLink"))
        mapOf(
            "top.mrxiaom.pluginbase" to "base",
            "de.tr7zw.changeme.nbtapi" to "nbtapi",
            "com.tcoded.folialib" to "folialib",
        ).forEach { (original, target) ->
            relocate(original, "$shadowGroup.$target")
        }
    }
}
