plugins {
    id("com.android.application") version "8.2.2"
    id("org.jetbrains.kotlin.android") version "1.9.22"
}

android {
    namespace = "com.prelightwight.aibrother"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.prelightwight.aibrother"
        minSdk = 21
        targetSdk = 28
        versionCode = 4  // Updated version
        versionName = "1.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Explicit ABI support for Xiaomi Mi 8
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a")
        }
        
        // Enable native build with llama.cpp integration
        externalNativeBuild {
            cmake {
                cppFlags += listOf("-std=c++17", "-frtti", "-fexceptions")
                abiFilters += listOf("arm64-v8a")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
        debug {
            isMinifyEnabled = false
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true  // Use view binding instead of Compose
    }

    // Enable native build configuration for llama.cpp
    externalNativeBuild {
        cmake {
            path = file("CMakeLists.txt")
            version = "3.22.1"
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            useLegacyPackaging = true
        }
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
        disable += "ExpiredTargetSdkVersion"
    }
}

dependencies {
    // Android dependencies
    implementation("androidx.core:core-ktx:1.8.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    
    // RecyclerView for chat interface
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    
    // Navigation and Fragments
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.0")
    implementation("androidx.fragment:fragment-ktx:1.6.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
}