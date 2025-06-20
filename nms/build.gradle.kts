// ========== 设置 ===========

val targetJavaVersion = 8
val source = NMSSource.RoseWoodDev
val sharedSpigotAPI = "1.13"

// ==========================

subprojects {
    apply(plugin="java")
    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven(source.repository)
    }
    dependencies {
        add("compileOnly", "org.jetbrains:annotations:24.0.0")

        extra.func<String>("setupNMS") { minecraft ->
            add("compileOnly", "org.spigotmc:spigot-api:$minecraft-R0.1-SNAPSHOT")
            add("compileOnly", source.dependency(minecraft))
        }
    }
    extra.func<Project, Int>("setupJava") { proj, javaVer ->
        proj.extensions.configure(JavaPluginExtension::class) {
            val ver = JavaVersion.toVersion(javaVer)
            if (JavaVersion.current() < ver) {
                val lang = JavaLanguageVersion.of(javaVer)
                toolchain.languageVersion.set(lang)
            }
            sourceCompatibility = ver
            targetCompatibility = ver
        }
        proj.tasks.withType<JavaCompile>().configureEach {
            options.encoding = "UTF-8"
            sourceCompatibility = javaVer.toString()
            targetCompatibility = javaVer.toString()
        }
    }
    setupJava(targetJavaVersion)
}
val shared = project("shared")
shared.dependencies {
    add("compileOnly", "org.spigotmc:spigot-api:$sharedSpigotAPI-R0.1-SNAPSHOT")
}
subprojects {
    dependencies {
        if (name.startsWith("v")) add("compileOnly", shared)
    }
}
object NMSSource {
    class Source(
        val repository: String,
        val dependency: (String) -> String,
    )
    val RoseWoodDev = Source(
        repository = "https://repo.rosewooddev.io/repository/public/",
        dependency = { "org.spigotmc:spigot:$it" }
    )
    val CodeMC = Source(
        repository = "https://repo.codemc.io/repository/nms/",
        dependency = { "org.spigotmc:spigot:$it-R0.1-SNAPSHOT" }
    )
}
