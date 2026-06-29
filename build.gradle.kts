plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.4"
}

group = "phphleb"
version = "1.2.1"

repositories {
    mavenCentral()
}

tasks.test {
    useJUnitPlatform()
}

// Конфигурация плагина IntelliJ
intellij {
    pluginName.set("hleb2-integration")
    localPath.set("/opt/PhpStorm") // использует локальную установку, без скачивания IDE
    plugins.set(listOf("com.jetbrains.php"))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<JavaCompile> {
    options.release.set(17)
}

tasks {
    buildSearchableOptions {
        enabled = false
    }
    patchPluginXml {
        sinceBuild.set("232")
        untilBuild.set(provider { null })
    }
}
