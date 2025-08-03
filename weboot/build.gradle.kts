plugins {
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.4"
    //kotlin("jvm") version "1.9.0" // if using Kotlin, else remove
    java
}

repositories{
    mavenCentral()
}


tasks.bootJar {
    archiveFileName = "weboot.jar"
    mainClass = "com.zencode.app.Application"
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web") // includes spring-mvc
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.postgresql:postgresql:42.7.3")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
    implementation("org.springframework.boot:spring-boot-starter-websocket")


    // Log4j 2 (optional; Spring Boot uses logback by default)
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")
    implementation("org.apache.logging.log4j:log4j-api:2.20.0")
    //implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.20.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21)) // Or whatever version you want
    }
}

