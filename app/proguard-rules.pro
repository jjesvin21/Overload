# Add project specific ProGuard rules here.
-keep class com.overloadtracker.data.local.entity.** { *; }
-keep class * extends androidx.room.RoomDatabase
-dontwarn retrofit2.**
