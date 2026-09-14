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

    val javaExtension = extensions.getByType<JavaPluginExtension>()
    javaExtension.toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }

    val integrationTestSourceSet = javaExtension.sourceSets.create("integrationTest") {
        compileClasspath += javaExtension.sourceSets["main"].output + javaExtension.sourceSets["test"].output
        runtimeClasspath += javaExtension.sourceSets["main"].output + javaExtension.sourceSets["test"].output
    }

    configurations.named("integrationTestImplementation") {
        extendsFrom(configurations["testImplementation"])
    }
    configurations.named("integrationTestRuntimeOnly") {
        extendsFrom(configurations["testRuntimeOnly"])
    }

    // Consistent snake_case JSON + UTC timestamps for all services
    tasks.withType<KotlinCompile> {
        compilerOptions { freeCompilerArgs.addAll("-Xjsr305=strict") }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging { events("passed", "skipped", "failed") }
        filter {
            isFailOnNoMatchingTests = false
        }
    }

    tasks.register<Test>("integrationTest") {
        description = "Runs integration tests"
        group       = "verification"
        testClassesDirs = integrationTestSourceSet.output.classesDirs
        classpath       = integrationTestSourceSet.runtimeClasspath
        useJUnitPlatform()
        filter {
            isFailOnNoMatchingTests = false
        }
        onlyIf {
            file("src/integrationTest").exists()
        }
        shouldRunAfter(tasks.named("test"))
    }
}


// -- Common config for shared library -----------------------------------------
configure(subprojects.filter { it.path.startsWith(":shared") }) {
    apply(plugin = "java-library")
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



