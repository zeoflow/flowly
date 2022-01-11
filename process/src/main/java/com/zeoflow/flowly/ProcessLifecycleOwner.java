package com.zeoflow.flowly;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.VisibleForTesting;

/**
 * Class that provides flowly for the whole application process.
 * <p>
 * You can consider this LifecycleOwner as the composite of all of your Activities, except that
 * {@link Lifecycle.Event#ON_CREATE} will be dispatched once and {@link Lifecycle.Event#ON_DESTROY}
 * will never be dispatched. Other flowly events will be dispatched with following rules:
 * ProcessLifecycleOwner will dispatch {@link Lifecycle.Event#ON_START},
 * {@link Lifecycle.Event#ON_RESUME} events, as a first activity moves through these events.
 * {@link Lifecycle.Event#ON_PAUSE}, {@link Lifecycle.Event#ON_STOP}, events will be dispatched with
 * a <b>delay</b> after a last activity
 * passed through them. This delay is long enough to guarantee that ProcessLifecycleOwner
 * won't send any events if activities are destroyed and recreated due to a
 * configuration change.
 *
 * <p>
 * It is useful for use cases where you would like to react on your app coming to the foreground or
 * going to the background and you don't need a milliseconds accuracy in receiving flowly
 * events.
 */
@SuppressWarnings("WeakerAccess")
public class ProcessLifecycleOwner implements LifecycleOwner {

    @VisibleForTesting
    static final long TIMEOUT_MS = 700; //mls

    // ground truth counters
    private int mStartedCounter = 0;
    private int mResumedCounter = 0;

    private boolean mPauseSent = true;
    private boolean mStopSent = true;

    private Handler mHandler;
    private final LifecycleRegistry mRegistry = new LifecycleRegistry(this);

    private final Runnable mDelayedPauseRunnable = () -> {
        dispatchPauseIfNeeded();
        dispatchStopIfNeeded();
    };

    ReportFragment.ActivityInitializationListener mInitializationListener =
            new ReportFragment.ActivityInitializationListener() {
                @Override
                public void onCreate() {
                }

                @Override
                public void onStart() {
                    activityStarted();
                }

                @Override
                public void onResume() {
                    activityResumed();
                }
            };

    private static final ProcessLifecycleOwner sInstance = new ProcessLifecycleOwner();

    /**
     * The LifecycleOwner for the whole application process. Note that if your application
     * has multiple processes, this provider does not know about other processes.
     *
     * @return {@link LifecycleOwner} for the whole application.
     */
    @NonNull
    public static LifecycleOwner get() {
        return sInstance;
    }

    /**
     * Adds a LifecycleObserver that will be notified when the LifecycleOwner changes
     * state.
     * <p>
     * The given observer will be brought to the current state of the LifecycleOwner.
     * For example, if the LifecycleOwner is in {@link Lifecycle.State#STARTED} state, the given observer
     * will receive {@link Lifecycle.Event#ON_CREATE}, {@link Lifecycle.Event#ON_START} events.
     *
     * @param observer The observer to notify.
     */
    public static void addObserver(@NonNull LifecycleObserver observer) {
        get().getLifecycle().addObserver(observer);
    }

    @NonNull
    public static ProcessLifecycleOwner getInstance() {
        return sInstance;
    }

    static void init(Context context) {
        sInstance.attach(context);
    }

    public void activityCreated() {
        mRegistry.dispatchEvent(
                Lifecycle.Event.ON_ACTIVITY_CREATED
        );
    }

    public void activityStarted() {
        mStartedCounter++;
        if (mStartedCounter == 1 && mStopSent) {
            mRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START);
            mStopSent = false;
            mRegistry.dispatchEvent(
                    Lifecycle.Event.ON_ACTIVITY_STARTED
            );
        }
    }

    public void activityResumed() {
        mResumedCounter++;
        if (mResumedCounter == 1) {
            if (mPauseSent) {
                mRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME);
                mPauseSent = false;
                mRegistry.dispatchEvent(
                        Lifecycle.Event.ON_ACTIVITY_RESUMED
                );
            } else {
                mHandler.removeCallbacks(mDelayedPauseRunnable);
            }
        }
    }

    public void activityPaused() {
        mResumedCounter--;
        if (mResumedCounter == 0) {
            mHandler.postDelayed(mDelayedPauseRunnable, TIMEOUT_MS);
            mRegistry.dispatchEvent(
                    Lifecycle.Event.ON_ACTIVITY_PAUSED
            );
        }
    }

    public void activityStopped() {
        mStartedCounter--;
        dispatchStopIfNeeded();
        mRegistry.dispatchEvent(
                Lifecycle.Event.ON_ACTIVITY_STOPPED
        );
    }

    public void activityDestroyed() {
        mRegistry.dispatchEvent(
                Lifecycle.Event.ON_ACTIVITY_DESTROYED
        );
    }

    void dispatchPauseIfNeeded() {
        if (mResumedCounter == 0) {
            mPauseSent = true;
            mRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE);
        }
    }

    void dispatchStopIfNeeded() {
        if (mStartedCounter == 0 && mPauseSent) {
            mRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP);
            mStopSent = true;
        }
    }

    public void applicationLaunched() {
        mRegistry.dispatchEvent(
                Lifecycle.Event.ON_APPLICATION_LAUNCHED,
                AppStartUp.getLoadingTime()
        );
    }

    public void applicationCreated() {
        mRegistry.dispatchEvent(
                Lifecycle.Event.ON_APPLICATION_CREATED
        );
    }

    public void applicationStarted() {
        mRegistry.dispatchEvent(
                Lifecycle.Event.ON_APPLICATION_STARTED
        );
    }

    public void applicationResumed() {
        mRegistry.dispatchEvent(
                Lifecycle.Event.ON_APPLICATION_RESUMED
        );
    }

    public void applicationPaused() {
        mRegistry.dispatchEvent(
                Lifecycle.Event.ON_APPLICATION_PAUSED
        );
    }

    public void applicationStopped() {
        mRegistry.dispatchEvent(
                Lifecycle.Event.ON_APPLICATION_STOPPED
        );
    }

    public void applicationDestroyed() {
        mRegistry.dispatchEvent(
                Lifecycle.Event.ON_APPLICATION_DESTROYED
        );
    }

    public void activityReady() {
        System.out.println("onActivityReady:value");
        mRegistry.dispatchEvent(
                Lifecycle.Event.ON_ACTIVITY_READY
        );
    }

    public void activityLoadTime(long time) {
        mRegistry.dispatchEvent(
                Lifecycle.Event.ON_ACTIVITY_CREATED,
                time
        );
    }

    private ProcessLifecycleOwner() {
    }

    void attach(Context context) {
        mHandler = new Handler();
        mRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);
        Application app = (Application) context.getApplicationContext();
        app.registerActivityLifecycleCallbacks(new EmptyActivityLifecycleCallbacks() {
            @RequiresApi(29)
            @Override
            public void onActivityPreCreated(@NonNull Activity activity,
                    @Nullable Bundle savedInstanceState) {
                // We need the ProcessLifecycleOwner to get ON_START and ON_RESUME precisely
                // before the first activity gets its LifecycleOwner started/resumed.
                // The activity's LifecycleOwner gets started/resumed via an activity registered
                // callback added in onCreate(). By adding our own activity registered callback in
                // onActivityPreCreated(), we get our callbacks first while still having the
                // right relative order compared to the Activity's onStart()/onResume() callbacks.
                activity.registerActivityLifecycleCallbacks(new EmptyActivityLifecycleCallbacks() {
                    @Override
                    public void onActivityPostStarted(@NonNull Activity activity) {
                        activityStarted();
                    }

                    @Override
                    public void onActivityPostResumed(@NonNull Activity activity) {
                        activityResumed();
                    }
                });
            }

            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                // Only use ReportFragment pre API 29 - after that, we can use the
                // onActivityPostStarted and onActivityPostResumed callbacks registered in
                // onActivityPreCreated()
                if (Build.VERSION.SDK_INT < 29) {
                    ReportFragment.get(activity).setProcessListener(mInitializationListener);
                }
            }

            @Override
            public void onActivityPaused(Activity activity) {
                activityPaused();
            }

            @Override
            public void onActivityStopped(Activity activity) {
                activityStopped();
            }
        });
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return mRegistry;
    }
}
