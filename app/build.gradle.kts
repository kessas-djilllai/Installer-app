import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
}

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.ZenixInstaller.app"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      // 1. Try environment variables first
      val envStoreFile = System.getenv("RELEASE_STORE_FILE") ?: System.getenv("KEYSTORE_PATH")
      val envStorePassword = System.getenv("RELEASE_STORE_PASSWORD") ?: System.getenv("STORE_PASSWORD")
      val envKeyAlias = System.getenv("RELEASE_KEY_ALIAS") ?: System.getenv("KEY_ALIAS")
      val envKeyPassword = System.getenv("RELEASE_KEY_PASSWORD") ?: System.getenv("KEY_PASSWORD")

      // 2. Try loading local.properties
      val localProperties = Properties().apply {
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
          localPropertiesFile.inputStream().use { load(it) }
        }
      }

      val propStoreFile = localProperties.getProperty("RELEASE_STORE_FILE")?.trim()
      val propStorePassword = localProperties.getProperty("RELEASE_STORE_PASSWORD")?.trim()
      val propKeyAlias = localProperties.getProperty("RELEASE_KEY_ALIAS")?.trim()
      val propKeyPassword = localProperties.getProperty("RELEASE_KEY_PASSWORD")?.trim()

      // 3. Determine the actual lookup/fallback mechanism
      val keystorePath = envStoreFile ?: propStoreFile ?: "my-upload-key.jks"

      // We list potential locations in order of preference
      val possibleFiles = listOf(
        if (file(keystorePath).isAbsolute) file(keystorePath) else rootProject.file(keystorePath),
        file(keystorePath),
        rootProject.file("default_keystore.jks"),
        file("default_keystore.jks"),
        rootProject.file("debug.keystore"),
        file("debug.keystore"),
        file("${rootDir}/debug.keystore")
      )

      val resolvedFile = possibleFiles.firstOrNull { it.exists() }

      if (resolvedFile != null) {
        storeFile = resolvedFile
        if (resolvedFile.name.contains("debug.keystore")) {
          storePassword = "android"
          keyAlias = "androiddebugkey"
          keyPassword = "android"
        } else if (resolvedFile.name.contains("default_keystore.jks")) {
          storePassword = envStorePassword ?: propStorePassword ?: "defaultKeystorePass123"
          keyAlias = envKeyAlias ?: propKeyAlias ?: "defaultAlias"
          keyPassword = envKeyPassword ?: propKeyPassword ?: "defaultKeyPass123"
        } else {
          storePassword = envStorePassword ?: propStorePassword ?: ""
          keyAlias = envKeyAlias ?: propKeyAlias ?: "upload"
          keyPassword = envKeyPassword ?: propKeyPassword ?: ""
        }
      } else {
        // Absolute fallback to a dummy configuration so configuration stage always completes
        val fallbackFile = rootProject.file("debug.keystore")
        storeFile = if (fallbackFile.exists()) fallbackFile else file("${rootDir}/debug.keystore")
        storePassword = "android"
        keyAlias = "androiddebugkey"
        keyPassword = "android"
      }
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      signingConfig = signingConfigs.getByName("debugConfig")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  // implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  // implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  // implementation(libs.firebase.ai)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}
