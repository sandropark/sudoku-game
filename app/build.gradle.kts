import org.yaml.snakeyaml.Yaml

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("jacoco")
}

buildscript {
    dependencies {
        classpath("org.yaml:snakeyaml:1.33")
    }
}

android {
    namespace = "com.sandro.new_sudoku"
    compileSdk = 35

    val configFile = file("../config/sudoku.yml")
    val config = if (configFile.exists()) {
        try {
            val yaml = Yaml()
            yaml.load(configFile.readText()) as Map<String, Any>
        } catch (e: Exception) {
            emptyMap()
        }
    } else {
        emptyMap()
    }

    defaultConfig {
        applicationId = "com.sandro.new_sudoku"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["coverage"] = "true"
        val appId = config["app-id"]?.toString() ?: ""
        manifestPlaceholders["adAppId"] = appId
    }

    buildTypes {
        debug {
            // Google AdMob 테스트 광고 단위 ID들
            buildConfigField(
                "String",
                "AD_APP_OPEN_UNIT_ID",
                "\"ca-app-pub-3940256099942544/9257395921\""
            )          // 앱 오프닝 광고
            buildConfigField(
                "String",
                "AD_BANNER_UNIT_ID",
                "\"ca-app-pub-3940256099942544/9214589741\""
            )             // 적응형 배너
            buildConfigField(
                "String",
                "AD_BANNER_FIXED_UNIT_ID",
                "\"ca-app-pub-3940256099942544/6300978111\""
            )       // 고정 크기 배너
            buildConfigField(
                "String",
                "AD_INTERSTITIAL_UNIT_ID",
                "\"ca-app-pub-3940256099942544/1033173712\""
            )       // 전면 광고
            buildConfigField(
                "String",
                "AD_REWARDED_UNIT_ID",
                "\"ca-app-pub-3940256099942544/5224354917\""
            )           // 보상형 광고
            buildConfigField(
                "String",
                "AD_REWARDED_INTERSTITIAL_UNIT_ID",
                "\"ca-app-pub-3940256099942544/5354046379\""
            ) // 보상형 전면 광고
            buildConfigField(
                "String",
                "AD_NATIVE_UNIT_ID",
                "\"ca-app-pub-3940256099942544/2247696110\""
            )             // 네이티브
            buildConfigField(
                "String",
                "AD_NATIVE_VIDEO_UNIT_ID",
                "\"ca-app-pub-3940256099942544/1044960115\""
            )       // 네이티브 동영상
        }
        release {
            // YAML 파일에서 광고 ID 로드
            val adIds = if (config.isNotEmpty()) {
                config["ad-id"] as? Map<String, String> ?: emptyMap()
            } else {
                emptyMap()
            }

            buildConfigField(
                "String",
                "AD_APP_OPEN_UNIT_ID",
                "\"${adIds["app-open"] ?: "ca-app-pub-3940256099942544/9257395921"}\""
            )          // 앱 오프닝 광고
            buildConfigField(
                "String",
                "AD_BANNER_UNIT_ID",
                "\"${adIds["banner"] ?: "ca-app-pub-3940256099942544/9214589741"}\""
            )             // 적응형 배너
            buildConfigField(
                "String",
                "AD_BANNER_FIXED_UNIT_ID",
                "\"${adIds["banner-fixed"] ?: "ca-app-pub-3940256099942544/6300978111"}\""
            )       // 고정 크기 배너
            buildConfigField(
                "String",
                "AD_INTERSTITIAL_UNIT_ID",
                "\"${adIds["interstitial"] ?: "ca-app-pub-3940256099942544/1033173712"}\""
            )       // 전면 광고
            buildConfigField(
                "String",
                "AD_REWARDED_UNIT_ID",
                "\"${adIds["rewarded"] ?: "ca-app-pub-3940256099942544/5224354917"}\""
            )           // 보상형 광고
            buildConfigField(
                "String",
                "AD_REWARDED_INTERSTITIAL_UNIT_ID",
                "\"${adIds["rewarded-interstitial"] ?: "ca-app-pub-3940256099942544/5354046379"}\""
            ) // 보상형 전면 광고
            buildConfigField(
                "String",
                "AD_NATIVE_UNIT_ID",
                "\"${adIds["native"] ?: "ca-app-pub-3940256099942544/2247696110"}\""
            )             // 네이티브
            buildConfigField(
                "String",
                "AD_NATIVE_VIDEO_UNIT_ID",
                "\"${adIds["native-video"] ?: "ca-app-pub-3940256099942544/1044960115"}\""
            )       // 네이티브 동영상

            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

jacoco {
    toolVersion = "0.8.11"
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    val fileFilter = listOf(
        // 필요시 제외할 파일 패턴 추가
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*"
    )
    val debugTree = fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug")) {
        exclude(fileFilter)
    }
    val mainSrc = "src/main/java"
    classDirectories.setFrom(debugTree)
    sourceDirectories.setFrom(files(mainSrc))
    executionData.setFrom(fileTree(layout.buildDirectory) {
        include("jacoco/testDebugUnitTest.exec")
    })
}

tasks.register<JacocoReport>("jacocoUiTestReport") {
    dependsOn("connectedDebugAndroidTest")
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*"
    )
    val debugTree = fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug")) {
        exclude(fileFilter)
    }
    val mainSrc = "src/main/java"
    classDirectories.setFrom(debugTree)
    sourceDirectories.setFrom(files(mainSrc))
    executionData.setFrom(fileTree(layout.buildDirectory.dir("outputs/code-coverage/connected")) {
        include("coverage.ec")
    })
}

tasks.register<JacocoReport>("jacocoCombinedReport") {
    dependsOn("testDebugUnitTest", "connectedDebugAndroidTest")
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*"
    )
    val debugTree = fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug")) {
        exclude(fileFilter)
    }
    val mainSrc = "src/main/java"
    classDirectories.setFrom(debugTree)
    sourceDirectories.setFrom(files(mainSrc))
    executionData.setFrom(
        fileTree(layout.buildDirectory) {
            include("jacoco/testDebugUnitTest.exec")
        },
        fileTree(layout.buildDirectory.dir("outputs/code-coverage/connected")) {
            include("coverage.ec")
        }
    )
}

tasks.register("uiTestReport") {
    dependsOn("connectedDebugAndroidTest")
    doLast {
        val reportFile =
            file("${layout.buildDirectory}/reports/androidTests/connected/debug/index.html")
        if (reportFile.exists()) {
            println("✅ UI 테스트 리포트 생성 완료: ${reportFile.absolutePath}")
            println("🌐 브라우저에서 확인: file://${reportFile.absolutePath}")
        } else {
            println("❌ UI 테스트 리포트를 찾을 수 없습니다.")
        }
    }
}

tasks.register("testAll") {
    dependsOn("testDebugUnitTest", "testReleaseUnitTest", "connectedDebugAndroidTest")
    description = "모든 테스트 실행 (단위 테스트 + UI 테스트)"
}

dependencies {
    implementation("com.google.android.gms:play-services-ads:24.5.0") // AdMob

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.1")

    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    testImplementation("androidx.arch.core:core-testing:2.2.0")

    // Kotest
    testImplementation("io.kotest:kotest-assertions-core:5.8.0")
    testImplementation("io.kotest:kotest-runner-junit5:5.8.0")
    androidTestImplementation("io.kotest:kotest-assertions-core:5.8.0")

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation("androidx.compose.foundation:foundation")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.7.8")
    debugImplementation("androidx.compose.ui:ui-tooling:1.7.8")
}