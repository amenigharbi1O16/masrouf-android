plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.masrouf"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.masrouf"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    constraints {
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.8.20") {
            because("kotlin-stdlib 1.8.20 includes jdk7")
        }
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.20") {
            because("kotlin-stdlib 1.8.20 includes jdk8")
        }
    }
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.8.20"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation("androidx.drawerlayout:drawerlayout:1.2.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}
