plugins {
    java
}
repositories {
    maven("https://repo.codemc.io/repository/maven-public/")
}
dependencies {
    compileOnly("net.kyori:adventure-api:4.22.0")
    compileOnly("net.kyori:adventure-platform-bukkit:4.4.0")
    compileOnly("de.tr7zw:item-nbt-api:2.15.3-SNAPSHOT")
}
