plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
    application
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

repositories {
    mavenCentral()
}

dependencies {
    val kotlinVersion: String by project
    val coroutinesVersion: String by project
    val ktorVersion: String by project
    val logbackVersion: String by project
    val kgraphqlVersion: String by project
    val koinVersion: String by project
    val exposedVersion: String by project
    val springBootVersion: String by project

    implementation(project(":domain:common"))
    implementation(project(":domain:auth"))
    implementation(project(":domain:user"))
    implementation(project(":domain:server"))
    implementation(project(":application"))

    implementation(kotlin("stdlib:$kotlinVersion"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-sessions:$ktorVersion")
    implementation("com.apurebase:kgraphql:$kgraphqlVersion")
    implementation("com.apurebase:kgraphql-ktor:$kgraphqlVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    implementation("io.insert-koin:koin-ktor:$koinVersion")

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    implementation("mysql:mysql-connector-java:8.0.20")

    implementation("org.springframework.boot:spring-boot-starter-security:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-mail:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-data-redis:$springBootVersion")

    testImplementation("io.ktor:ktor-server-tests:$ktorVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("com.h2database:h2:1.4.200")
}

tasks.withType<Jar> {
    manifest {
        attributes(
            mapOf(
                "Main-Class" to application.mainClass.get()
            )
        )
    }
}

