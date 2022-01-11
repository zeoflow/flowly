-keepattributes *Annotation*

-keepclassmembers enum com.zeoflow.flowly.Lifecycle$Event {
    <fields>;
}

-keep !interface * implements com.zeoflow.flowly.LifecycleObserver {
}

-keep class * implements com.zeoflow.flowly.GeneratedAdapter {
    <init>(...);
}

-keepclassmembers class ** {
    @com.zeoflow.flowly.OnLifecycleEvent *;
}

# this rule is need to work properly when app is compiled with api 28, see b/142778206
# Also this rule prevents registerIn from being inlined.
-keepclassmembers class com.zeoflow.flowly.ReportFragment$LifecycleCallbacks { *; }