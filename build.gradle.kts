plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.4"
}

group = "phphleb"
version = "1.0.0"

repositories {
    mavenCentral()
}

tasks.test {
    useJUnitPlatform()
}

// Конфигурация плагина IntelliJ
intellij {
    pluginName.set("hleb2-integration")
    version.set("2023.2") // Минимальная версия IDE
    type.set("PS")
    plugins.set(listOf(
            // https://plugins.jetbrains.com/plugin/6610-php/versions
            "com.jetbrains.php:232.8660.205",
            // https://plugins.jetbrains.com/plugin/7511-php-remote-interpreter/versions
            "org.jetbrains.plugins.phpstorm-remote-interpreter:232.8660.142",
    ))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks {
    buildSearchableOptions {
        enabled = false
    }
    patchPluginXml {
        sinceBuild.set("232")
        untilBuild.set("252.*")
    }
}
