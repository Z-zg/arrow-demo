plugins {
    kotlin("jvm") version "2.0.21"
    id("maven-publish")
    id("jacoco")
    id("io.gitlab.arturbosch.detekt") version "1.23.6"
}

group = project.findProperty("group") as String
version = project.findProperty("version") as String

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    // Arrow Core
    implementation("io.arrow-kt:arrow-core:${project.findProperty("arrowVersion")}")
    implementation("io.arrow-kt:arrow-fx-coroutines:${project.findProperty("arrowVersion")}")
    implementation("io.arrow-kt:arrow-optics:${project.findProperty("arrowVersion")}")
    
    // Kotlin标准库
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    // 测试依赖
    testImplementation("org.junit.jupiter:junit-jupiter:${project.findProperty("junitVersion")}")
    testImplementation("io.kotest:kotest-runner-junit5:${project.findProperty("kotestVersion")}")
    testImplementation("io.kotest:kotest-assertions-core:${project.findProperty("kotestVersion")}")
    testImplementation("io.kotest:kotest-property:${project.findProperty("kotestVersion")}")
    testImplementation("io.mockk:mockk:${project.findProperty("mockkVersion")}")
}

kotlin {
    jvmToolchain(21)
    
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xjsr305=strict",
            "-Xcontext-receivers"
        )
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
    
    // 并行测试执行
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1
}

jacoco {
    toolVersion = project.findProperty("jacocoVersion") as String
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
    
    finalizedBy(tasks.jacocoTestCoverageVerification)
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}

detekt {
    config.setFrom("$projectDir/detekt.yml")
    buildUponDefaultConfig = true
    autoCorrect = true
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(true)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            
            pom {
                name.set("KtArrow Library")
                description.set("Kotlin Arrow函数式编程库示例项目")
                url.set("https://github.com/example/ktarrow-lib")
                
                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
                
                developers {
                    developer {
                        id.set("developer")
                        name.set("Developer Name")
                        email.set("developer@example.com")
                    }
                }
            }
        }
    }
}