# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# --- Правила для моделей данных --- 
# Сохраняем классы моделей данных и их члены (поля, методы)
# Убедитесь, что com.example.readingroom.model - это правильный пакет
-keep class com.example.readingroom.model.** { *; }
-keepclassmembers class com.example.readingroom.model.** { *; }

# --- Правила для Kotlin --- 
# Правила для Kotlin Coroutines (если используете активно, особенно с Flow/StateFlow в ViewModels)
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.flow.** { *; }
-keep class kotlinx.coroutines.flow.StateFlowImpl { *; }

# Правила для Kotlin Reflect (если используется где-то неявно)
-dontwarn kotlin.reflect.jvm.internal.**

# --- Правила для аннотаций --- 
# Сохраняем аннотации @Keep (если будете использовать для каких-то классов)
-keep,allowobfuscation @interface androidx.annotation.Keep
-keep @androidx.annotation.Keep class *
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# --- Правила для отладки --- 
# Сохраняем информацию о номерах строк для стектрейсов (полезно для отладки крешей)
-keepattributes SourceFile,LineNumberTable

# --- Остальные правила (примеры, пока закомментированы) --- 

# Если ваш проект использует WebView с JS, раскомментируйте следующее
# и укажите полное имя класса интерфейса JavaScript
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Если вы сохраняете информацию о номерах строк, раскомментируйте это, чтобы
# скрыть исходное имя файла.
#-renamesourcefileattribute SourceFile

# Правила для Cloudinary, скорее всего, НЕ НУЖНЫ, так как библиотека предоставляет consumer rules.
# -keep class com.cloudinary.android.*Strategy
# -dontwarn com.cloudinary.**

# Правила для Retrofit/OkHttp (часто уже включены в optimize.txt)
# -dontwarn okio.**
# -dontwarn retrofit2.Platform$Java8
# -keepattributes Signature
# -keepattributes InnerClasses