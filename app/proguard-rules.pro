# Debug builds do not use this file. Rules below only matter for release builds.
-keepattributes *Annotation*, InnerClasses, Signature
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**
