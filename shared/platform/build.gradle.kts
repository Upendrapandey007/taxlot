dependencies {
    api("org.springframework.boot:spring-boot-starter-web:3.3.3")
    api("org.springframework.boot:spring-boot-starter-validation:3.3.3")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.2")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
}

tasks.bootJar { enabled = false }
tasks.jar { enabled = true }
