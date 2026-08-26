plugins {
  alias(libs.plugins.android.application)
}

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.aistudio.miuiwhitelist.hook"
    minSdk = 24
    targetSdk = 36
    versionCode = 2
    versionName = "1.0.1"
  }

  signingConfigs {
    create("debugConfig") {
      val keystoreFile = listOf(
        file("${rootDir}/keystore.jks"),
        file("${rootDir}/debug.keystore"),
        file("keystore.jks"),
        file("debug.keystore")
      ).firstOrNull { it.exists() } ?: file("${rootDir}/debug.keystore")

      storeFile = keystoreFile
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
      enableV1Signing = true
      enableV2Signing = true
      enableV3Signing = true
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("debugConfig")
    }
    debug {
      isMinifyEnabled = false
      signingConfig = signingConfigs.getByName("debugConfig")
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  buildFeatures {
    compose = false
    buildConfig = false
  }

  dependenciesInfo {
    includeInApk = false
    includeInBundle = false
  }
}

dependencies {
  compileOnly("de.robv.android.xposed:api:82")
}
