# JLPT Quest ProGuard / R8 rules
# Keep Room generated code and entities
-keep class com.itsmesujan.jlpt.data.local.** { *; }
# Keep Billing client models
-keep class com.android.billingclient.** { *; }
# Keep enum values used by navigation routes / content
-keepclassmembers enum com.itsmesujan.jlpt.** { public static **[] values(); public static ** valueOf(java.lang.String); }
# Strip verbose logging in release
-assumenosideeffects class android.util.Log { public static int v(...); public static int d(...); }
-dontwarn org.bouncycastle.**
