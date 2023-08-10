plugins {
  id("com.android.library")
  id("org.jetbrains.kotlin.android")
}

android {
  namespace = "org.markensic.commonlib"
  compileSdk = 33

  defaultConfig {
    minSdk = 24

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    consumerProguardFiles("consumer-rules.pro")
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
  kotlinOptions {
    jvmTarget = "1.8"
  }
  sourceSets {
    val main by getting
    main.java.srcDirs("src/main/java")
    main.kotlin.srcDirs("src/main/kotlin")
  }
}

dependencies {
  val kotlin_version = "1.8.10"
  val kotlinx_version = "1.6.1"

  implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlin_version")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinx_version")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$kotlinx_version")
  implementation("androidx.core:core-ktx:1.10.1")
  implementation("androidx.appcompat:appcompat:1.6.1")
  implementation(platform("com.squareup.okio:okio-bom:3.5.0"))
  testImplementation("junit:junit:4.13.2")
  androidTestImplementation("androidx.test.ext:junit:1.1.5")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
