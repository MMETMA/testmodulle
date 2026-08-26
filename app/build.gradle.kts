import java.util.Base64

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
    versionCode = 6
    versionName = "1.0.5"
  }

  signingConfigs {
    create("debugConfig") {
      val keystoreFile = file("${rootDir}/debug.keystore")
      if (!keystoreFile.exists() || keystoreFile.length() == 0L) {
        val base64File = file("${rootDir}/debug.keystore.base64")
        if (base64File.exists()) {
          val cleanB64 = base64File.readText().replace("\\s+".toRegex(), "")
          val decoded = Base64.getDecoder().decode(cleanB64)
          keystoreFile.writeBytes(decoded)
        }
      }
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
