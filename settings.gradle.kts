rootProject.name = "SweetLocks"

include(":nms")
File("nms").listFiles()?.forEach { file ->
    if (File(file, "build.gradle.kts").exists()) {
        include(":nms:${file.name}")
    }
}
