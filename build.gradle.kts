plugins {
    id("fabric-loom") version "1.7-SNAPSHOT"
}

java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17
java.toolchain.languageVersion = JavaLanguageVersion.of(17)
group = "ru.vidtu.bscfsio"
base.archivesName = "BSCFSIO-Fabric-1.20.1"

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
    maven("https://maven.terraformersmc.com/releases/")
    maven("https://maven.shedaniel.me/")
}

dependencies {
    minecraft("com.mojang:minecraft:1.20.1")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:0.16.2")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.92.2+1.20.1")
    modImplementation("com.terraformersmc:modmenu:7.2.2")
    modImplementation("me.shedaniel.cloth:cloth-config-fabric:11.1.118")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-g", "-parameters"))
    options.release = 17
}

tasks.withType<ProcessResources> {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

tasks.withType<Jar> {
    from(rootDir.resolve("LICENSE"))
    manifest {
        attributes(
            "Specification-Title" to "BlockShiftClickingForSomeItemsOnly",
            "Specification-Version" to project.version,
            "Specification-Vendor" to "VidTu",
            "Implementation-Title" to "BSCFSIO-Fabric-1.20.1",
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "VidTu"
        )
    }
}
