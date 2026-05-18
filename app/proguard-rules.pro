# ──────────────────────────────────────────────────────────────────────────────
# Stack traces — keep file/line info so Crashlytics shows readable traces
# ──────────────────────────────────────────────────────────────────────────────
-keepattributes SourceFile,LineNumberTable
-keepattributes Signature,InnerClasses,EnclosingMethod
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations
-renamesourcefileattribute SourceFile

# ──────────────────────────────────────────────────────────────────────────────
# Gson — keep all data classes used in toJson/fromJson
# ──────────────────────────────────────────────────────────────────────────────
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep all our serializable model classes (Gson uses reflection on these)
-keep class com.ninthbalcony.pushuprpg.data.model.** { *; }
-keep class com.ninthbalcony.pushuprpg.utils.QuestDef { *; }
-keep class com.ninthbalcony.pushuprpg.utils.ActiveQuest { *; }
-keep class com.ninthbalcony.pushuprpg.utils.AchievementDef { *; }
-keep enum com.ninthbalcony.pushuprpg.utils.QuestType { *; }

# Keep nested Gson wrapper classes inside utils objects (e.g. ItemUtils$ItemsWrapper)
-keep class com.ninthbalcony.pushuprpg.utils.ItemUtils$* { *; }

# ──────────────────────────────────────────────────────────────────────────────
# Room — keep entities, DAOs, and database
# ──────────────────────────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-dontwarn androidx.room.paging.**

-keep class com.ninthbalcony.pushuprpg.data.db.** { *; }

# ──────────────────────────────────────────────────────────────────────────────
# Firebase Realtime Database — getValue(Class) needs reflection
# ──────────────────────────────────────────────────────────────────────────────
-keepattributes *Annotation*
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Our Firebase-serialized classes
-keep class com.ninthbalcony.pushuprpg.data.repository.LeaderboardEntry { *; }

# ──────────────────────────────────────────────────────────────────────────────
# Play Games Services v2
# ──────────────────────────────────────────────────────────────────────────────
-keep class com.google.android.gms.games.** { *; }
-dontwarn com.google.android.gms.**

# ──────────────────────────────────────────────────────────────────────────────
# AdMob
# ──────────────────────────────────────────────────────────────────────────────
-keep class com.google.android.gms.ads.** { *; }

# ──────────────────────────────────────────────────────────────────────────────
# Coroutines
# ──────────────────────────────────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.flow.**

# ──────────────────────────────────────────────────────────────────────────────
# Lottie
# ──────────────────────────────────────────────────────────────────────────────
-keep class com.airbnb.lottie.** { *; }
-dontwarn com.airbnb.lottie.**

# ──────────────────────────────────────────────────────────────────────────────
# Coil 2.x — подкладывает свои consumer-rules через AAR; добавляем safety
# net для OkHttp (Coil использует его как Call.Factory) и Okio.
# ──────────────────────────────────────────────────────────────────────────────
-dontwarn coil.**
-dontwarn okhttp3.**
-dontwarn okio.**

# ──────────────────────────────────────────────────────────────────────────────
# Kotlin metadata — required for reflection-heavy libs (Compose runtime, etc.)
# ──────────────────────────────────────────────────────────────────────────────
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings { <fields>; }

# Keep Kotlin data class component functions (used by Compose remember, etc.)
-keepclassmembers class * {
    public static ** Companion;
}

# ──────────────────────────────────────────────────────────────────────────────
# Compose — usually safe with default rules, but explicitly skip warnings
# ──────────────────────────────────────────────────────────────────────────────
-dontwarn androidx.compose.**
