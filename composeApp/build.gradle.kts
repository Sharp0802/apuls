import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

val populateCommands = tasks.register("populateCommands") {
    val csvFile = file("$projectDir/src/commonMain/codegen/commands.csv")
    val outputDir = file("$projectDir/src/commonMain/kotlin/com.apulsetech.apuls2/command")
    val outputFile = File(outputDir, "Commands.kt")

    inputs.file(csvFile)
    outputs.file(outputFile)

    doLast {
        if (!csvFile.exists()) {
            println("CSV file not found, skipping generation.")
            return@doLast
        }

        val content = StringBuilder()
        content.append("@file:Suppress(\"EnumEntryName\", \"SpellCheckingInspection\", \"unused\")\n\n")
        content.append("package com.apulsetech.apuls2.command\n\n")
        content.append("import com.apulsetech.apuls2.data.*\n\n")
        content.append("enum class CommandDeclarations(val value: CommandDeclaration) {\n")

        val cmd = mutableListOf<String>()
        val pCmd = mutableListOf<String>()
        csvFile.forEachLine { line ->
            if (line.startsWith("#")) {
                return@forEachLine
            }

            val parts = line.split(",")
            if (parts.size < 5) {
                return@forEachLine
            }

            // val category = parts[0].trim()
            val name = parts[1].trim()
            val label = parts[2].trim()
            val type = parts[3].trim()
            val constraint = parts[4].trim()
            val readonly = parts[5].trim() == "readonly"

            val declaration = if (type.isEmpty()) {
                cmd.add(name)
                "    $name(CommandDeclaration(\"$name\", \"$label\")),\n"
            } else {
                pCmd.add(name)
                "    $name(CommandDeclaration.parameterized<$type>(\"$name\", \"$label\", $readonly, arrayOf($constraint))),\n"
            }

            content.append(declaration)
        }

        content.append("    ;\n")

        content.append("    companion object {\n")
        content.append("        val commands = arrayOf(${cmd.joinToString(", ") { v -> "$v.value" }})\n")
        content.append("        val parameterizedCommands = arrayOf(${pCmd.joinToString(", ") { v -> "$v.value as ParameterizedCommandDeclaration" }})\n")
        content.append("    }\n")

        content.append("}\n")

        // 4. Write to file
        if (!outputDir.exists()) outputDir.mkdirs()
        outputFile.writeText(content.toString())

        println("Generated file at: ${outputFile.absolutePath}")
    }
}

tasks.named("preBuild") {
    dependsOn(populateCommands)
}

tasks.withType<KotlinCompile>().configureEach {
    dependsOn(populateCommands)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    jvm()
    
    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(libs.filekit.core)
            implementation(libs.filekit.dialogs)
            implementation(libs.filekit.dialogs.compose)
            implementation(libs.filekit.coil)
            implementation(libs.material3)
            compileOnly(libs.jakarta.mail)
            implementation(compose.materialIconsExtended)
            implementation(libs.navigation.compose)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(libs.jakarta.mail)
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }
    }
}

android {
    namespace = "com.apulsetech.apuls2"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.apulsetech.apuls2"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "com.apulsetech.apuls2.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.apulsetech.apuls2"
            packageVersion = "1.0.0"
        }
    }
}
