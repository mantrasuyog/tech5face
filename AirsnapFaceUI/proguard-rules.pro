# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile


-keep class ai.tech5.pheonix.capture.controller.**
#-keep class com.face.liveness.passive.net.facesdk.**
#-keep class com.face.liveness.passive.android.**
#-keep class com.face.liveness.passive.android.media**
#-keep class net.idrnd.facesdk.**

-keepclassmembers class ai.tech5.pheonix.capture.controller.* {
    <fields>;
    <init>();
    <methods>;
}
#-keepclassmembers class com.face.liveness.BuildConfig {
#    <fields>;
#    <init>();
#    <methods>;
#}
#-keepclassmembers class com.face.liveness.model.* {
#    <fields>;
#    <init>();
#    <methods>;
#}

#-keepclassmembers class com.face.liveness.utils.* {
#    <fields>;
#    <init>();
#    <methods>;
#}


#-keepclassmembers class net.idrnd.facesdk.* {
#    <fields>;
#    <init>();
#    <methods>;
#}