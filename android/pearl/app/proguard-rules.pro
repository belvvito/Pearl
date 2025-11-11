# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Compose правила
-keep class androidx.compose.runtime.Composer { *; }
-keep class androidx.compose.runtime.ComposerKt { *; }

# Coil правила
-keep class com.beutystore.pearl.BuildConfig { *; }
-keep class coil.** { *; }
-dontwarn coil.**

# Retrofit/Gson правила
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod

# OkHttp правила - УСИЛЕННЫЕ
-keep class okhttp3.** { *; }
-keep class okio.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# Material3 правила
-keep class androidx.compose.material3.** { *; }

# Navigation правила
-keep class androidx.navigation.** { *; }

# Kotlin правила
-keep class kotlin.** { *; }
-dontwarn kotlin.**
-keep class kotlinx.** { *; }
-dontwarn kotlinx.**

# AndroidX правила
-keep class androidx.** { *; }
-dontwarn androidx.**

# Coroutines правила
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Gson правила
-keep class com.google.gson.** { *; }
-keep class com.google.gson.stream.** { *; }
-dontwarn com.google.gson.**

# Retrofit правила
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Жизненный цикл и ViewModel
-keep class androidx.lifecycle.** { *; }
-keep class * extends androidx.lifecycle.ViewModel

# Room правила (если используется)
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.Entity

# Для отладки - сохраняем имена методов
-keepattributes LineNumberTable,SourceFile
-renamesourcefileattribute SourceFile

# Сохраняем R классы
-keep class **.R
-keep class **.R$* {
    <fields>;
}

# Сохраняем BuildConfig
-keep class **.BuildConfig

# Сохраняем нативные методы
-keepclasseswithmembers class * {
    native <methods>;
}

# Сохраняем методы обратного вызова
-keepclassmembers class * {
    void on*(**);
}

# Предотвращаем удаление пустых конструкторов
-keepclassmembers class * {
    public <init>();
}