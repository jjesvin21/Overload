# Add project specific ProGuard rules here.
-keep class com.overloadtracker.data.local.entity.** { *; }
-keep class * extends androidx.room.RoomDatabase
-dontwarn retrofit2.**

# Hilt & Dagger keep rules
-keep class dagger.hilt.** { *; }
-keep class com.overloadtracker.**_GeneratedInjector { *; }
-keep class com.overloadtracker.Dagger* { *; }
-keepclassmembers class * {
    @javax.inject.Inject *;
}

