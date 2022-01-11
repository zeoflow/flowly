# Flowly
Helps you manage your app states easier.

### 1. Depend on our library

Memo Library is available through Maven Repository.
To use it:

1.  Open the `build.gradle` file for your application.
2.  Make sure that the `repositories` section includes Maven Repository
    `mavenCentral()`. For example:
```groovy
  allprojects {
    repositories {
      mavenCentral()
    }
  }
```

3.  Add the library to the `dependencies` section:
```groovy
dependencies {
    // ...

    // declare version
    def version = "x.y.z"
    // Library Implementation
    implementation("com.zeoflow.flowly:process:$version")
    implementation("com.zeoflow.flowly:common:$version")
    implementation("com.zeoflow.flowly:runtime:$version")
    annotationProcessor("com.zeoflow.flowly:compiler:$version")
    // For kotlin projects use kapt instead of annotationProcessor
    kapt("com.zeoflow.flowly:compiler:$version")

    // ...
}