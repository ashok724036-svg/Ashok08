# Keep Room entities
-keep class com.neetquest.neetquestsaver.data.entity.** { *; }
# Keep Gson serialization
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
# Keep Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
