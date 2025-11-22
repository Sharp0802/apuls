import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

tasks.register("populateCommands") {
    val csvFile = file("$projectDir/src/main/res/codegen/commands.csv")
    val outputDir = file("$projectDir/src/main/kotlin/com/apulsetech/apuls/command")
    val outputFile = File(outputDir, "Commands.kt")

    inputs.file(csvFile)
    outputs.file(outputFile)

    doLast {
        if (!csvFile.exists()) {
            println("CSV file not found, skipping generation.")
            return@doLast
        }

        val content = StringBuilder()
        content.append("/// <generated />\n\n")
        content.append("package com.apulsetech.apuls.command\n\n")
        content.append("import com.apulsetech.apuls.data.*\n\n")
        content.append("enum class CommandDeclarations(value: CommandDeclaration) {\n")

        csvFile.forEachLine { line ->
            val parts = line.split(",")
            if (parts.size < 5) {
                return@forEachLine
            }

            // val category = parts[0].trim()
            val name = parts[1].trim()
            val label = parts[2].trim()
            val type = parts[3].trim()
            val constraint = parts[4].trim()

            val declaration = if (type.isEmpty()) {
                "    $name(CommandDeclaration(\"$name\", \"$label\")),\n"
            } else {
                "    $name(CommandDeclaration.parameterized<$type>(\"$name\", \"$label\", arrayOf($constraint))),\n"
            }

            content.append(declaration)
        }

        content.append("}\n")

        // 4. Write to file
        if (!outputDir.exists()) outputDir.mkdirs()
        outputFile.writeText(content.toString())

        println("Generated file at: ${outputFile.absolutePath}")
    }
}

tasks.named("preBuild") {
    dependsOn("populateCommands")
}

android {
    namespace = "com.apulsetech.apuls"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.apulsetech.apuls"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        jvmToolchain(11)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.usb.serial.for1.android)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.ui.text)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}