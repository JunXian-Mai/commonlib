plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
}

android {
  namespace = "org.markensic.commonlib.demo"
  compileSdk = 34

  defaultConfig {
    applicationId = "org.markensic.commonlib.demo"
    minSdk = 26
    targetSdk = 34
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }

  signingConfigs {
    create("release") {
      storeFile = file("../release.keystore")
      storePassword = "markensic@ooi"
      keyAlias = "markensic"
      keyPassword = "markensic@iio"
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
  implementation(project(":commonlib"))
  implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
  implementation("androidx.datastore:datastore-preferences:1.1.1")
  implementation("androidx.work:work-runtime:2.9.0")
  implementation("androidx.work:work-runtime-ktx:2.9.0")
  implementation("androidx.work:work-multiprocess:2.9.0")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
  implementation("com.squareup.okio:okio:3.7.0")
  runtimeOnly("com.squareup.okio:okio-jvm:3.7.0")

  implementation("androidx.core:core-ktx:1.13.1")
  implementation("androidx.activity:activity-ktx:1.9.0")
  implementation("androidx.appcompat:appcompat:1.7.0")
  implementation("com.google.android.material:material:1.12.0")
  testImplementation("junit:junit:4.13.2")
  androidTestImplementation("androidx.test.ext:junit:1.1.5")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
