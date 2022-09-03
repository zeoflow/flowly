package com.zeoflow.flowly;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.GuardedBy;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.lifecycle.ViewModelStore;
import androidx.lifecycle.ViewModelStoreOwner;


import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

@SuppressWarnings("unused")
public class ApplicationManager implements Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {

    @SuppressLint("StaticFieldLeak")
    @GuardedBy("LOCK")
    private static final ApplicationManager INSTANCE = new ApplicationManager();
    private static final Object LOCK = new Object();

    private final List<String> activitiesCreated = new ArrayList<>();
    private final WeakHashMap<String, ApplicationObserver> mApplicationObservers = new WeakHashMap<>();

    private WeakReference<Activity> currentActivity;
    private WeakReference<Activity> lastActivity;
    private WeakReference<AppCompatActivity> compatActivity;
    private WeakReference<FragmentManager> mFragmentManager;

    private boolean firstLaunch = true;
    private long startUpTime = 0;

    protected ApplicationManager() {

    }

    public static ApplicationManager init(Application application) {
        application.registerActivityLifecycleCallbacks(getInstance());
        ProcessLifecycleOwner.get().getLifecycle().addObserver(getInstance());
        return getInstance();
    }

    public static void addObserver(ApplicationObserver applicationObserver) {
        getInstance().mApplicationObservers.put(applicationObserver.getClass().getName(), applicationObserver);
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

    @Nullable
    public static LifecycleOwner getFlowlyOwner() {
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
        Log.d("AppManager", "onActivityPreCreated " + activity.getClass());
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        Log.d("AppManager", "onActivityCreated " + activity.getClass());
        if (currentActivity == null) {
            lastActivity = new WeakReference<>(activity);
            currentActivity = new WeakReference<>(activity);
        }
        if (activity instanceof AppCompatActivity) {
            compatActivity = new WeakReference<>((AppCompatActivity) activity);
            mFragmentManager = new WeakReference<>(((AppCompatActivity) activity).getSupportFragmentManager());
        }
        startUpTime = System.currentTimeMillis();
    }

    @Override
    public void onActivityPostCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
    }

    @Override
    public void onActivityPreStarted(@NonNull Activity activity) {
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
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
        }
        if (activity instanceof AppCompatActivity) {
            compatActivity = new WeakReference<>((AppCompatActivity) activity);
            mFragmentManager = new WeakReference<>(((AppCompatActivity) activity).getSupportFragmentManager());
        }
        startUpTime = 0;
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
    }

    @Override
    public void onActivityPostPaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivityPreStopped(@NonNull Activity activity) {
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
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
        currentActivity = null;
        activitiesCreated.remove(activity.getClass().getName());
        compatActivity = null;
    }

    @Override
    public void onActivityPostDestroyed(@NonNull Activity activity) {
    }

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onCreate(owner);
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onStart(owner);
    }

    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onResume(owner);
        if (firstLaunch) {
            for (ApplicationObserver observers: mApplicationObservers.values()) {
                observers.onApplicationCreate();
            }
            firstLaunch = false;
        }
    }

    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onPause(owner);
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onStop(owner);
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onDestroy(owner);
    }
}