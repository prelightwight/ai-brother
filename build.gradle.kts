plugins {
    id("com.android.application") version "8.2.2"
    id("org.jetbrains.kotlin.android") version "1.9.22"
}

android {
    namespace = "com.prelightwight.aibrother"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.prelightwight.aibrother"
        minSdk = 26  // Updated for POI libraries compatibility
        targetSdk = 28
        versionCode = 4  // Updated version
        versionName = "1.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Explicit ABI support for Xiaomi Mi 8
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a")
        }
        
        // Enable native build with llama.cpp integration - temporarily disabled
        // externalNativeBuild {
        //     cmake {
        //         cppFlags += listOf("-std=c++17", "-frtti", "-fexceptions")
        //         abiFilters += listOf("arm64-v8a")
        //     }
        // }
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

    // Enable native build configuration for llama.cpp - temporarily disabled
    // externalNativeBuild {
    //     cmake {
    //         path = file("CMakeLists.txt")
    //     }
    // }

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
    
    // JSON serialization for conversation persistence
    implementation("com.google.code.gson:gson:2.10.1")
    
    // File handling and document processing
    implementation("androidx.activity:activity-ktx:1.8.0")
    implementation("androidx.fragment:fragment-ktx:1.6.1")
    
    // Permissions handling
    implementation("androidx.core:core:1.12.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    // Document processing libraries
    implementation("org.apache.poi:poi:5.2.4")
    implementation("org.apache.poi:poi-ooxml:5.2.4")
    implementation("org.apache.poi:poi-scratchpad:5.2.4")
    
    // PDF processing
    implementation("com.itextpdf:itext7-core:7.2.5")
    
    // Image processing
    implementation("androidx.exifinterface:exifinterface:1.3.6")

    // Testing
    testImplementation("junit:junit:4.13.2")
}