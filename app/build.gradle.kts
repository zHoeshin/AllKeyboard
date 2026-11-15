plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "nn.zhoeshin.allkeyboard"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "nn.zhoeshin.allkeyboard"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("com.github.h0tk3y.betterParse:better-parse:0.4.4")
    implementation("com.ernestoyaquello.dragdropswiperecyclerview:drag-drop-swipe-recyclerview:1.2.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.apache.commons:commons-text:1.12.0")
    implementation("androidx.gridlayout:gridlayout:1.0.0")
    implementation("androidx.core:core-ktx:1.12.0")
    //implementation("com.github.AmrDeveloper:CodeView:1.3.9")
    implementation("androidx.activity:activity-ktx:1.10.1")
}