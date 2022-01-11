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
 * You can consider this FlowlyOwner as the composite of all of your Activities, except that
 * {@link Flowly.Event#ON_CREATE} will be dispatched once and {@link Flowly.Event#ON_DESTROY}
 * will never be dispatched. Other flowly events will be dispatched with following rules:
 * ProcessFlowlyOwner will dispatch {@link Flowly.Event#ON_START},
 * {@link Flowly.Event#ON_RESUME} events, as a first activity moves through these events.
 * {@link Flowly.Event#ON_PAUSE}, {@link Flowly.Event#ON_STOP}, events will be dispatched with
 * a <b>delay</b> after a last activity
 * passed through them. This delay is long enough to guarantee that ProcessFlowlyOwner
 * won't send any events if activities are destroyed and recreated due to a
 * configuration change.
 *
 * <p>
 * It is useful for use cases where you would like to react on your app coming to the foreground or
 * going to the background and you don't need a milliseconds accuracy in receiving flowly
 * events.
 */
@SuppressWarnings("WeakerAccess")
public class ProcessFlowlyOwner implements FlowlyOwner {

    @VisibleForTesting
    static final long TIMEOUT_MS = 700; //mls

    // ground truth counters
    private int mStartedCounter = 0;
    private int mResumedCounter = 0;

    private boolean mPauseSent = true;
    private boolean mStopSent = true;

    private Handler mHandler;
    private final FlowlyRegistry mRegistry = new FlowlyRegistry(this);

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

    private static final ProcessFlowlyOwner sInstance = new ProcessFlowlyOwner();

    /**
     * The FlowlyOwner for the whole application process. Note that if your application
     * has multiple processes, this provider does not know about other processes.
     *
     * @return {@link FlowlyOwner} for the whole application.
     */
    @NonNull
    public static FlowlyOwner get() {
        return sInstance;
    }

    /**
     * Adds a LifecycleObserver that will be notified when the FlowlyOwner changes
     * state.
     * <p>
     * The given observer will be brought to the current state of the FlowlyOwner.
     * For example, if the FlowlyOwner is in {@link Flowly.State#STARTED} state, the given observer
     * will receive {@link Flowly.Event#ON_CREATE}, {@link Flowly.Event#ON_START} events.
     *
     * @param observer The observer to notify.
     */
    public static void addObserver(@NonNull FlowlyObserver observer) {
        get().getLifecycle().addObserver(observer);
    }

    @NonNull
    public static ProcessFlowlyOwner getInstance() {
        return sInstance;
    }

    static void init(Context context) {
        sInstance.attach(context);
    }

    public void activityCreated() {
        mRegistry.dispatchEvent(
                Flowly.Event.ON_ACTIVITY_CREATED
        );
    }

    public void activityStarted() {
        mStartedCounter++;
        if (mStartedCounter == 1 && mStopSent) {
            mRegistry.handleLifecycleEvent(Flowly.Event.ON_START);
            mStopSent = false;
            mRegistry.dispatchEvent(
                    Flowly.Event.ON_ACTIVITY_STARTED
            );
        }
    }

    public void activityResumed() {
        mResumedCounter++;
        if (mResumedCounter == 1) {
            if (mPauseSent) {
                mRegistry.handleLifecycleEvent(Flowly.Event.ON_RESUME);
                mPauseSent = false;
                mRegistry.dispatchEvent(
                        Flowly.Event.ON_ACTIVITY_RESUMED
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
                    Flowly.Event.ON_ACTIVITY_PAUSED
            );
        }
    }

    public void activityStopped() {
        mStartedCounter--;
        dispatchStopIfNeeded();
        mRegistry.dispatchEvent(
                Flowly.Event.ON_ACTIVITY_STOPPED
        );
    }

    public void activityDestroyed() {
        mRegistry.dispatchEvent(
                Flowly.Event.ON_ACTIVITY_DESTROYED
        );
    }

    void dispatchPauseIfNeeded() {
        if (mResumedCounter == 0) {
            mPauseSent = true;
            mRegistry.handleLifecycleEvent(Flowly.Event.ON_PAUSE);
        }
    }

    void dispatchStopIfNeeded() {
        if (mStartedCounter == 0 && mPauseSent) {
            mRegistry.handleLifecycleEvent(Flowly.Event.ON_STOP);
            mStopSent = true;
        }
    }

    public void applicationLaunched() {
        mRegistry.dispatchEvent(
                Flowly.Event.ON_APPLICATION_LAUNCHED,
                AppStartUp.getLoadingTime()
        );
    }

    public void applicationCreated() {
        mRegistry.dispatchEvent(
                Flowly.Event.ON_APPLICATION_CREATED
        );
    }

    public void applicationStarted() {
        mRegistry.dispatchEvent(
                Flowly.Event.ON_APPLICATION_STARTED
        );
    }

    public void applicationResumed() {
        mRegistry.dispatchEvent(
                Flowly.Event.ON_APPLICATION_RESUMED
        );
    }

    public void applicationPaused() {
        mRegistry.dispatchEvent(
                Flowly.Event.ON_APPLICATION_PAUSED
        );
    }

    public void applicationStopped() {
        mRegistry.dispatchEvent(
                Flowly.Event.ON_APPLICATION_STOPPED
        );
    }

    public void applicationDestroyed() {
        mRegistry.dispatchEvent(
                Flowly.Event.ON_APPLICATION_DESTROYED
        );
    }

    public void activityReady() {
        System.out.println("onActivityReady:value");
        mRegistry.dispatchEvent(
                Flowly.Event.ON_ACTIVITY_READY
        );
    }

    public void activityLoadTime(long time) {
        mRegistry.dispatchEvent(
                Flowly.Event.ON_ACTIVITY_CREATED,
                time
        );
    }

    private ProcessFlowlyOwner() {
    }

    void attach(Context context) {
        mHandler = new Handler();
        mRegistry.handleLifecycleEvent(Flowly.Event.ON_CREATE);
        Application app = (Application) context.getApplicationContext();
        app.registerActivityLifecycleCallbacks(new EmptyActivityFlowlyCallbacks() {
            @RequiresApi(29)
            @Override
            public void onActivityPreCreated(@NonNull Activity activity,
                    @Nullable Bundle savedInstanceState) {
                // We need the ProcessFlowlyOwner to get ON_START and ON_RESUME precisely
                // before the first activity gets its FlowlyOwner started/resumed.
                // The activity's FlowlyOwner gets started/resumed via an activity registered
                // callback added in onCreate(). By adding our own activity registered callback in
                // onActivityPreCreated(), we get our callbacks first while still having the
                // right relative order compared to the Activity's onStart()/onResume() callbacks.
                activity.registerActivityLifecycleCallbacks(new EmptyActivityFlowlyCallbacks() {
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
    public Flowly getLifecycle() {
        return mRegistry;
    }
}
