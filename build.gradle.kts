/*
 * MIT License
 *
 * Copyright (c) 2024-2025 VidTu
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

plugins {
    alias(libs.plugins.architectury.loom)
}

java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17
java.toolchain.languageVersion = JavaLanguageVersion.of(17)

group = "ru.vidtu.bscfsio"
base.archivesName = "BSCFSIO-Fabric-1.20.1"
description = "Mod that sometimes somehow block somewhat resembling clicking inventory by someone."

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
    maven("https://maven.terraformersmc.com/releases/")
    maven("https://maven.shedaniel.me/")
}

loom {
    silentMojangMappingsLicense()
    runs.named("client") {
        vmArgs(
            // Allow JVM without hotswap to work.
            "-XX:+IgnoreUnrecognizedVMOptions",

            // Set up RAM.
            "-Xmx2G",

            // Allow hot swapping on supported JVM.
            "-XX:+AllowEnhancedClassRedefinition",
            "-XX:+AllowRedefinitionToAddDeleteMethods",
            "-XX:HotswapAgent=fatjar",
            "-Dfabric.debug.disableClassPathIsolation=true"
        )
    }
    @Suppress("UnstableApiUsage")
    mixin {
        defaultRefmapName = "bscfsio.mixins.refmap.json"
    }
}

dependencies {
    // Minecraft
    minecraft(libs.minecraft)
    mappings(loom.officialMojangMappings())

    // Fabric
    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.api)
    modImplementation(libs.modmenu)
    modImplementation(libs.cloth.config)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-g", "-parameters"))
    options.release = 17
}

tasks.withType<ProcessResources> {
    inputs.property("version", version)
    filesMatching(listOf("fabric.mod.json", "quilt.mod.json")) {
        expand(inputs.properties)
    }
}

tasks.withType<AbstractArchiveTask> {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

tasks.withType<Jar> {
    from(rootDir.resolve("LICENSE"))
    manifest {
        attributes(
            "Specification-Title" to "BSCFSIO",
            "Specification-Version" to version,
            "Specification-Vendor" to "VidTu",
            "Implementation-Title" to "BSCFSIO-Fabric-1.20.1",
            "Implementation-Version" to version,
            "Implementation-Vendor" to "VidTu"
        )
    }
}
