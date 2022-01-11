package com.zeoflow.flowly;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.GuardedBy;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelStore;
import androidx.lifecycle.ViewModelStoreOwner;

import com.zeoflow.flowly.debug.Logger;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class ApplicationManager implements Application.ActivityLifecycleCallbacks, LifecycleObserver {

    private static final Logger logger = new Logger(
            "ApplicationManager"
    );

    @SuppressLint("StaticFieldLeak")
    @GuardedBy("LOCK")
    private static final ApplicationManager INSTANCE = new ApplicationManager();
    private static final Object LOCK = new Object();
    private WeakReference<Activity> currentActivity;
    private WeakReference<Activity> lastActivity;
    private WeakReference<AppCompatActivity> compatActivity;
    private WeakReference<FragmentManager> mFragmentManager;
    private boolean firstLaunch = true;
    private long startUpTime = 0;
    private final List<String> activitiesCreated = new ArrayList<>();

    protected ApplicationManager() {
        logger.d(Constants.INITIALIZED);
    }

    public static void init(Application application) {
        application.registerActivityLifecycleCallbacks(getInstance());
        ProcessLifecycleOwner.addObserver(getInstance());
    }

    @NonNull
    private static ApplicationManager getInstance() {
        synchronized (LOCK) {
            return INSTANCE;
        }
    }

    @NonNull
    public static Activity getActivity() {
        return getInstance().currentActivity != null ?
                getInstance().currentActivity.get() : getInstance().lastActivity.get();
    }

    @Nullable
    public static AppCompatActivity getCompatActivity() {
        if (getInstance().compatActivity != null ) {
            return getInstance().compatActivity.get();
        }
        return null;
    }

    @Nullable
    public static Activity getBaseActivity() {
        if (getCompatActivity() != null) {
            return getCompatActivity();
        }
        return getActivity();
    }

    @NonNull
    public static ViewModelStoreOwner getViewModelStoreOwner() {
        if (getCompatActivity() != null) {
            return getCompatActivity();
        }
        if (getActivity() instanceof ViewModelStoreOwner) {
            return (ViewModelStoreOwner) getActivity();
        }
        return ViewModelStore::new;
    }

    @Nullable
    public static LifecycleOwner getLifecycleOwner() {
        if (getCompatActivity() != null) {
            return getCompatActivity();
        }
        if (getActivity() instanceof LifecycleOwner) {
            return (LifecycleOwner) getActivity();
        }
        return null;
    }

    @NonNull
    public static FragmentManager getFragmentManager() {
        return getInstance().mFragmentManager.get();
    }

    @Override
    public void onActivityPreCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        if (currentActivity == null) {
            lastActivity = new WeakReference<>(activity);
        }
        if (activity instanceof AppCompatActivity) {
            compatActivity = new WeakReference<>((AppCompatActivity) activity);
            mFragmentManager = new WeakReference<>(((AppCompatActivity) activity).getSupportFragmentManager());
        }
        startUpTime = System.currentTimeMillis();
        ProcessLifecycleOwner.getInstance().activityCreated();
    }

    @Override
    public void onActivityPostCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
    }

    @Override
    public void onActivityPreStarted(@NonNull Activity activity) {
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        ProcessLifecycleOwner.getInstance().activityStarted();
    }

    @Override
    public void onActivityPostStarted(@NonNull Activity activity) {
    }

    @Override
    public void onActivityPreResumed(@NonNull Activity activity) {
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        currentActivity = new WeakReference<>(activity);
        if (!activitiesCreated.contains(activity.getClass().getName())) {
            activitiesCreated.add(activity.getClass().getName());
            ProcessLifecycleOwner.getInstance().activityLoadTime(System.currentTimeMillis() - startUpTime);
            ProcessLifecycleOwner.getInstance().activityReady();
        }
        if (activity instanceof AppCompatActivity) {
            compatActivity = new WeakReference<>((AppCompatActivity) activity);
            mFragmentManager = new WeakReference<>(((AppCompatActivity) activity).getSupportFragmentManager());
        }
        startUpTime = 0;
        ProcessLifecycleOwner.getInstance().activityResumed();
    }

    @Override
    public void onActivityPostResumed(@NonNull Activity activity) {
    }

    @Override
    public void onActivityPrePaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        lastActivity = new WeakReference<>(activity);
        ProcessLifecycleOwner.getInstance().activityPaused();
    }

    @Override
    public void onActivityPostPaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivityPreStopped(@NonNull Activity activity) {
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        ProcessLifecycleOwner.getInstance().activityStopped();
    }

    @Override
    public void onActivityPostStopped(@NonNull Activity activity) {
    }

    @Override
    public void onActivityPreSaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityPostSaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityPreDestroyed(@NonNull Activity activity) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        ProcessLifecycleOwner.getInstance().activityDestroyed();
        currentActivity = null;
        activitiesCreated.remove(activity.getClass().getName());
        compatActivity = null;
    }

    @Override
    public void onActivityPostDestroyed(@NonNull Activity activity) {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void onCreate() {
        ProcessLifecycleOwner.getInstance().applicationCreated();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onStart() {
        if (firstLaunch) {
            AppStartUp.appLoaded();
            ProcessLifecycleOwner.getInstance().applicationLaunched();
            firstLaunch = false;
        }
        ProcessLifecycleOwner.getInstance().applicationStarted();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() {
        ProcessLifecycleOwner.getInstance().applicationResumed();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause() {
        ProcessLifecycleOwner.getInstance().applicationPaused();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onStop() {
        ProcessLifecycleOwner.getInstance().applicationStopped();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        ProcessLifecycleOwner.getInstance().applicationDestroyed();
    }

}
