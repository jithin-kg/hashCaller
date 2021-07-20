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

##########################Room library#################################

-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
#Move all packages to under O pakage
-repackageclasses 'o'
#Code to enable optimiZation
-optimizations   code/simplification/arithmetic,!code/simplification/cast,!field/*,! class/merging/*,!method/inlining/*
-optimizationpasses 5
-allowaccessmodification

#########################Retrofit#################################

# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retrofit does reflection on method and parameter annotations.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit

# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.KotlinExtensions

# With R8 full mode, it sees no subtypes of Retrofit interfaces since they are created with a Proxy
# and replaces all potential values with null. Explicitly keeping the interfaces prevents this.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

#####################oKhttp######################

# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**

# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*

# OkHttp platform used only on JVM and when Conscrypt dependency is available.
-dontwarn okhttp3.internal.platform.ConscryptPlatform

#######################okio########################################

# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*



############################### glide #########################################
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
#-keep class android.telecom.TelecomManager {
#  *;
#}
#-keep  class android.telecom.TelecomManager*
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
  *** rewind();
}
#-------------------------------end--------------------------------------------

#this actually saved me from all errors, but lets see
-keepclassmembers class * extends com.google**{
 <fields>;
}



#https://medium.com/androiddevelopers/practical-proguard-rules-examples-5640a3907dc9

##okhtttp
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

#to retain source file names and line numbers for stack traces to make debugging easier:
-keepattributes SourceFile, LineNumberTable

#Sometimes it’s required to retain type information and annotations that are read at runtime, as opposed to compile time
-keepattributes *Annotation*, Signature, Exception


-keepclassmembers com.nibble.hashcaller.view.ui.contacts.utils.Constants
-keepclassmembers com.nibble.hashcaller.view.ui.sms.individual.util.constants