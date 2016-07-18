# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\Users\Administrator\AppData\Local\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-libraryjars'D:\Program Files\Java\jdk1.8.0_91\jre\lib\rt.jar'
-libraryjars'D:\Users\Administrator\AppData\Local\Android\sdk\platforms\android-24\android.jar'
-optimizationpasses 5

-dontusemixedcaseclassnames

-keep public class * com.longying.mylibrary.BeaconDeviceManager
-keep public class * com.longying.mylibrary.BeaconScanner
-keep public class * com.longying.mylibrary.BeaconScannerListener

-keep class com.longying.mylibrary.* {

public <fields>;

public <methods>;
}
