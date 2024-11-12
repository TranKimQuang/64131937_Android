// File build.gradle ở cấp dự án
buildscript {
    repositories {
        google()  // Đảm bảo đã thêm repository google()
        mavenCentral()
    }
    dependencies {
        classpath ("com.google.gms:google-services:4.3.15")  // Phiên bản mới nhất
    }
}
