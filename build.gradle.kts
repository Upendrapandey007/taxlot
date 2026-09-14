import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)           apply false
    alias(libs.plugins.kotlin.spring)        apply false
    alias(libs.plugins.spring.boot)          apply false
    alias(libs.plugins.spring.dep.mgmt)      apply false
}

allprojects {
    group   = "com.taxlot"
    version = "0.1.0-SNAPSHOT"
}

// -- Common config for every Spring Boot service ------------------------------
configure(subprojects.filter { it.path.startsWith(":services") }) {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")

    configure<JavaPluginExtension> {
        toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
    }

    // Consistent snake_case JSON + UTC timestamps for all services
    tasks.withType<KotlinCompile> {
        compilerOptions { freeCompilerArgs.addAll("-Xjsr305=strict") }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging { events("passed", "skipped", "failed") }
    }

    // Integration tests source set
    sourceSets.create("integrationTest") {
        compileClasspath += sourceSets["main"].output + sourceSets["test"].output
        runtimeClasspath += sourceSets["main"].output + sourceSets["test"].output
    }
    configurations["integrationTestImplementation"]
        .extendsFrom(configurations["testImplementation"])
    configurations["integrationTestRuntimeOnly"]
        .extendsFrom(configurations["testRuntimeOnly"])

    tasks.register<Test>("integrationTest") {
        description = "Runs integration tests"
        group       = "verification"
        testClassesDirs = sourceSets["integrationTest"].output.classesDirs
        classpath       = sourceSets["integrationTest"].runtimeClasspath
        useJUnitPlatform()
        shouldRunAfter(tasks["test"])
    }
}

// -- Common config for shared library -----------------------------------------
configure(subprojects.filter { it.path.startsWith(":shared") }) {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")

    configure<JavaPluginExtension> {
        toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
    }

    tasks.withType<KotlinCompile> {
        compilerOptions { freeCompilerArgs.addAll("-Xjsr305=strict") }
    }

    tasks.withType<Test> { useJUnitPlatform() }
}

