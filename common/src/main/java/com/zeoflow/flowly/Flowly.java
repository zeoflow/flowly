package com.zeoflow.flowly;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Defines an object that has an Android Lifecycle. {@link androidx.fragment.app.Fragment Fragment}
 * and {@link androidx.fragment.app.FragmentActivity FragmentActivity} classes implement
 * {@link FlowlyOwner} interface which has the {@link FlowlyOwner#getLifecycle()
 * getLifecycle} method to access the Lifecycle. You can also implement {@link FlowlyOwner}
 * in your own classes.
 * <p>
 * {@link Event#ON_CREATE}, {@link Event#ON_START}, {@link Event#ON_RESUME} events in this class
 * are dispatched <b>after</b> the {@link FlowlyOwner}'s related method returns.
 * {@link Event#ON_PAUSE}, {@link Event#ON_STOP}, {@link Event#ON_DESTROY} events in this class
 * are dispatched <b>before</b> the {@link FlowlyOwner}'s related method is called.
 * For instance, {@link Event#ON_START} will be dispatched after
 * {@link android.app.Activity#onStart onStart} returns, {@link Event#ON_STOP} will be dispatched
 * before {@link android.app.Activity#onStop onStop} is called.
 * This gives you certain guarantees on which state the owner is in.
 * <p>
 * If you use <b>Java 8 Language</b>, then observe events with {@link DefaultLifecycleObserver}.
 * To include it you should add {@code "com.zeoflow.flowly:flowly-common-java8:<version>"} to
 * your build.gradle file.
 * <pre>
 * class TestObserver implements DefaultLifecycleObserver {
 *     {@literal @}Override
 *     public void onCreate(FlowlyOwner owner) {
 *         // your code
 *     }
 * }
 * </pre>
 * If you use <b>Java 7 Language</b>, Lifecycle events are observed using annotations.
 * Once Java 8 Language becomes mainstream on Android, annotations will be deprecated, so between
 * {@link DefaultLifecycleObserver} and annotations,
 * you must always prefer {@code DefaultLifecycleObserver}.
 * <pre>
 * class TestObserver implements LifecycleObserver {
 *   {@literal @}OnLifecycleEvent(ON_STOP)
 *   void onStopped() {}
 * }
 * </pre>
 * <p>
 * Observer methods can receive zero or one argument.
 * If used, the first argument must be of type {@link FlowlyOwner}.
 * Methods annotated with {@link Event#ON_ANY} can receive the second argument, which must be
 * of type {@link Event}.
 * <pre>
 * class TestObserver implements LifecycleObserver {
 *   {@literal @}OnLifecycleEvent(ON_CREATE)
 *   void onCreated(FlowlyOwner source) {}
 *   {@literal @}OnLifecycleEvent(ON_ANY)
 *   void onAny(FlowlyOwner source, Event event) {}
 * }
 * </pre>
 * These additional parameters are provided to allow you to conveniently observe multiple providers
 * and events without tracking them manually.
 */
public abstract class Flowly {

    /**
     * Lifecycle coroutines extensions stashes the CoroutineScope into this field.
     *
     * @hide used by flowly-common-ktx
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    @NonNull
    AtomicReference<Object> mInternalScopeRef = new AtomicReference<>();

    /**
     * Adds a LifecycleObserver that will be notified when the FlowlyOwner changes
     * state.
     * <p>
     * The given observer will be brought to the current state of the FlowlyOwner.
     * For example, if the FlowlyOwner is in {@link State#STARTED} state, the given observer
     * will receive {@link Event#ON_CREATE}, {@link Event#ON_START} events.
     *
     * @param observer The observer to notify.
     */
    @MainThread
    public abstract void addObserver(@NonNull FlowlyObserver observer);

    /**
     * Removes the given observer from the observers list.
     * <p>
     * If this method is called while a state change is being dispatched,
     * <ul>
     * <li>If the given observer has not yet received that event, it will not receive it.
     * <li>If the given observer has more than 1 method that observes the currently dispatched
     * event and at least one of them received the event, all of them will receive the event and
     * the removal will happen afterwards.
     * </ul>
     *
     * @param observer The observer to be removed.
     */
    @MainThread
    public abstract void removeObserver(@NonNull FlowlyObserver observer);

    /**
     * Returns the current state of the Lifecycle.
     *
     * @return The current state of the Lifecycle.
     */
    @MainThread
    @NonNull
    public abstract State getCurrentState();

    @SuppressWarnings("WeakerAccess")
    public enum Event {
        /**
         * Constant for when the application was created.
         */
        ON_APPLICATION_LAUNCHED,
        /**
         * Constant for when the application was created.
         */
        ON_APPLICATION_CREATED,
        /**
         * Constant for onCreate event of the Application.
         */
        ON_APPLICATION_STARTED,
        /**
         * Constant for onCreate event of the Application.
         */
        ON_APPLICATION_RESUMED,
        /**
         * Constant for onCreate event of the Application.
         */
        ON_APPLICATION_PAUSED,
        /**
         * Constant for onCreate event of the Application.
         */
        ON_APPLICATION_STOPPED,
        /**
         * Constant for onCreate event of the Application.
         */
        ON_APPLICATION_DESTROYED,
        /**
         * Constant for when the application was created.
         */
        ON_ACTIVITY_CREATED,
        /**
         * Constant for onCreate event of the Application.
         */
        ON_ACTIVITY_STARTED,
        /**
         * Constant for when the application was created.
         */
        ON_ACTIVITY_READY,
        /**
         * Constant for onCreate event of the Application.
         */
        ON_ACTIVITY_RESUMED,
        /**
         * Constant for onCreate event of the Application.
         */
        ON_ACTIVITY_PAUSED,
        /**
         * Constant for onCreate event of the Application.
         */
        ON_ACTIVITY_STOPPED,
        /**
         * Constant for onCreate event of the Application.
         */
        ON_ACTIVITY_DESTROYED,
        /**
         * Constant for onCreate event of the {@link FlowlyOwner}.
         */
        ON_CREATE,
        /**
         * Constant for onStart event of the {@link FlowlyOwner}.
         */
        ON_START,
        /**
         * Constant for onResume event of the {@link FlowlyOwner}.
         */
        ON_RESUME,
        /**
         * Constant for onPause event of the {@link FlowlyOwner}.
         */
        ON_PAUSE,
        /**
         * Constant for onStop event of the {@link FlowlyOwner}.
         */
        ON_STOP,
        /**
         * Constant for onDestroy event of the {@link FlowlyOwner}.
         */
        ON_DESTROY,
        /**
         * An {@link Event Event} constant that can be used to match all events.
         */
        ON_ANY;

        /**
         * Returns the {@link Flowly.Event} that will be reported by a {@link Flowly}
         * leaving the specified {@link Flowly.State} to a lower state, or {@code null}
         * if there is no valid event that can move down from the given state.
         *
         * @param state the higher state that the returned event will transition down from
         * @return the event moving down the flowly phases from state
         */
        @Nullable
        public static Event downFrom(@NonNull State state) {
            switch (state) {
                case CREATED:
                    return ON_DESTROY;
                case STARTED:
                    return ON_STOP;
                case RESUMED:
                    return ON_PAUSE;
                default:
                    return null;
            }
        }

        /**
         * Returns the {@link Flowly.Event} that will be reported by a {@link Flowly}
         * entering the specified {@link Flowly.State} from a higher state, or {@code null}
         * if there is no valid event that can move down to the given state.
         *
         * @param state the lower state that the returned event will transition down to
         * @return the event moving down the flowly phases to state
         */
        @Nullable
        public static Event downTo(@NonNull State state) {
            switch (state) {
                case DESTROYED:
                    return ON_DESTROY;
                case CREATED:
                    return ON_STOP;
                case STARTED:
                    return ON_PAUSE;
                default:
                    return null;
            }
        }

        /**
         * Returns the {@link Flowly.Event} that will be reported by a {@link Flowly}
         * leaving the specified {@link Flowly.State} to a higher state, or {@code null}
         * if there is no valid event that can move up from the given state.
         *
         * @param state the lower state that the returned event will transition up from
         * @return the event moving up the flowly phases from state
         */
        @Nullable
        public static Event upFrom(@NonNull State state) {
            switch (state) {
                case INITIALIZED:
                    return ON_CREATE;
                case CREATED:
                    return ON_START;
                case STARTED:
                    return ON_RESUME;
                default:
                    return null;
            }
        }

        /**
         * Returns the {@link Flowly.Event} that will be reported by a {@link Flowly}
         * entering the specified {@link Flowly.State} from a lower state, or {@code null}
         * if there is no valid event that can move up to the given state.
         *
         * @param state the higher state that the returned event will transition up to
         * @return the event moving up the flowly phases to state
         */
        @Nullable
        public static Event upTo(@NonNull State state) {
            switch (state) {
                case CREATED:
                    return ON_CREATE;
                case STARTED:
                    return ON_START;
                case RESUMED:
                    return ON_RESUME;
                default:
                    return null;
            }
        }

        /**
         * Returns the new {@link Flowly.State} of a {@link Flowly} that just reported
         * this {@link Flowly.Event}.
         *
         * Throws {@link IllegalArgumentException} if called on {@link #ON_ANY}, as it is a special
         * value used by {@link OnFlowlyEvent} and not a real flowly event.
         *
         * @return the state that will result from this event
         */
        @NonNull
        public State getTargetState() {
            switch (this) {
                case ON_CREATE:
                case ON_STOP:
                    return State.CREATED;
                case ON_START:
                case ON_PAUSE:
                    return State.STARTED;
                case ON_RESUME:
                    return State.RESUMED;
                case ON_DESTROY:
                    return State.DESTROYED;
                case ON_ANY:
                    break;
            }
            throw new IllegalArgumentException(this + " has no target state");
        }

        /**
         * Returns the new {@link Flowly.State} of a {@link Flowly} that just reported
         * this {@link Flowly.Event}.
         *
         * Throws {@link IllegalArgumentException} if called on {@link #ON_ANY}, as it is a special
         * value used by {@link OnFlowlyEvent} and not a real flowly event.
         *
         * @return whether the event is for an application
         */
        public boolean isApplicationEvent() {
            return this.equals(Event.ON_APPLICATION_LAUNCHED) ||
                    this.equals(Event.ON_APPLICATION_CREATED) ||
                    this.equals(Event.ON_APPLICATION_STARTED) ||
                    this.equals(Event.ON_APPLICATION_RESUMED) ||
                    this.equals(Event.ON_APPLICATION_PAUSED) ||
                    this.equals(Event.ON_APPLICATION_STOPPED) ||
                    this.equals(Event.ON_APPLICATION_DESTROYED);
        }

        /**
         * Returns the new {@link Flowly.State} of a {@link Flowly} that just reported
         * this {@link Flowly.Event}.
         *
         * Throws {@link IllegalArgumentException} if called on {@link #ON_ANY}, as it is a special
         * value used by {@link OnFlowlyEvent} and not a real flowly event.
         *
         * @return whether the event is for an activity
         */
        public boolean isActivityEvent() {
            return this.equals(Event.ON_ACTIVITY_CREATED) ||
                    this.equals(Event.ON_ACTIVITY_STARTED) ||
                    this.equals(Event.ON_ACTIVITY_RESUMED) ||
                    this.equals(Event.ON_ACTIVITY_READY) ||
                    this.equals(Event.ON_ACTIVITY_PAUSED) ||
                    this.equals(Event.ON_ACTIVITY_STOPPED) ||
                    this.equals(Event.ON_ACTIVITY_DESTROYED);
        }
    }

    /**
     * Lifecycle states. You can consider the states as the nodes in a graph and
     * {@link Event}s as the edges between these nodes.
     */
    @SuppressWarnings("WeakerAccess")
    public enum State {
        /**
         * Destroyed state for a FlowlyOwner. After this event, this Lifecycle will not dispatch
         * any more events. For instance, for an {@link android.app.Activity}, this state is reached
         * <b>right before</b> Activity's {@link android.app.Activity#onDestroy() onDestroy} call.
         */
        DESTROYED,

        /**
         * Initialized state for a FlowlyOwner. For an {@link android.app.Activity}, this is
         * the state when it is constructed but has not received
         * {@link android.app.Activity#onCreate(android.os.Bundle) onCreate} yet.
         */
        INITIALIZED,

        /**
         * Created state for a FlowlyOwner. For an {@link android.app.Activity}, this state
         * is reached in two cases:
         * <ul>
         *     <li>after {@link android.app.Activity#onCreate(android.os.Bundle) onCreate} call;
         *     <li><b>right before</b> {@link android.app.Activity#onStop() onStop} call.
         * </ul>
         */
        CREATED,

        /**
         * Started state for a FlowlyOwner. For an {@link android.app.Activity}, this state
         * is reached in two cases:
         * <ul>
         *     <li>after {@link android.app.Activity#onStart() onStart} call;
         *     <li><b>right before</b> {@link android.app.Activity#onPause() onPause} call.
         * </ul>
         */
        STARTED,

        /**
         * Resumed state for a FlowlyOwner. For an {@link android.app.Activity}, this state
         * is reached after {@link android.app.Activity#onResume() onResume} is called.
         */
        RESUMED;

        /**
         * Compares if this State is greater or equal to the given {@code state}.
         *
         * @param state State to compare with
         * @return true if this State is greater or equal to the given {@code state}
         */
        public boolean isAtLeast(@NonNull State state) {
            return compareTo(state) >= 0;
        }
    }
}
