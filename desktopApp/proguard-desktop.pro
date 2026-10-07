-keep class com.schoolstats.** { *; }
-keep class org.koin.** { *; }
-keep class app.cash.sqldelight.** { *; }
-keep class org.sqlite.** { *; }
-keep class io.github.jan.supabase.** { *; }
-keep class io.ktor.** { *; }
-keep class org.apache.poi.** { *; }
-keep class com.lowagie.** { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-dontwarn **
