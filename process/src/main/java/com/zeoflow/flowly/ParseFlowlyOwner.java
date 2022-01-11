package com.zeoflow.flowly;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import java.lang.ref.WeakReference;
import java.util.HashMap;

public class ParseFlowlyOwner {

    public static FlowlyOwner parse(AppCompatActivity compatActivity) {
        return getFlowlyOwner(compatActivity);
    }

    private static FlowlyOwner getFlowlyOwner(AppCompatActivity compatActivity) {
        String name = compatActivity.getClass().getName();
        WeakReference<FlowlyOwner> pair = mFlowlyOwners.get(name);
        if (pair != null) {
            return pair.get();
        }
        return parseFlowlyOwner(compatActivity);
    }

    private static FlowlyOwner parseFlowlyOwner(AppCompatActivity compatActivity) {
        String name = compatActivity.getClass().getName();
        Lifecycle lifecycle = ((LifecycleOwner) compatActivity).getLifecycle();
        lifecycle.addObserver(new DefaultLifecycleObserver() {
            Flowly.State currentState = Flowly.State.CREATED;
            @Override
            public void onCreate(@NonNull LifecycleOwner owner) {
                currentState = Flowly.State.CREATED;
            }

            @Override
            public void onStart(@NonNull LifecycleOwner owner) {
                currentState = Flowly.State.STARTED;
            }

            @Override
            public void onResume(@NonNull LifecycleOwner owner) {
                currentState = Flowly.State.RESUMED;
            }

            @Override
            public void onPause(@NonNull LifecycleOwner owner) {
                currentState = Flowly.State.STARTED;
            }

            @Override
            public void onStop(@NonNull LifecycleOwner owner) {
                currentState = Flowly.State.CREATED;
            }

            @Override
            public void onDestroy(@NonNull LifecycleOwner owner) {
                currentState = Flowly.State.DESTROYED;
            }
        });
        FlowlyOwner flowlyOwner = () -> new Flowly() {

            @Override
            public void addObserver(@NonNull FlowlyObserver observer) {
                lifecycle.addObserver((LifecycleObserver) observer);
            }

            @Override
            public void removeObserver(@NonNull FlowlyObserver observer) {

            }

            @NonNull
            @Override
            public State getCurrentState() {
                return State.CREATED;
            }
        };
        mFlowlyOwners.put(name, new WeakReference<>(flowlyOwner));
        return flowlyOwner;
    }

    private Flowly.State currentState = Flowly.State.CREATED;

    FlowlyObserver flowlyObserver = new DefaultFlowlyObserver() {
        @Override
        public void onCreate(@NonNull FlowlyOwner owner) {
            DefaultFlowlyObserver.super.onCreate(owner);
        }

        @Override
        public void onStart(@NonNull FlowlyOwner owner) {
            DefaultFlowlyObserver.super.onStart(owner);
        }

        @Override
        public void onResume(@NonNull FlowlyOwner owner) {
            DefaultFlowlyObserver.super.onResume(owner);
        }

        @Override
        public void onPause(@NonNull FlowlyOwner owner) {
            DefaultFlowlyObserver.super.onPause(owner);
        }

        @Override
        public void onStop(@NonNull FlowlyOwner owner) {
            DefaultFlowlyObserver.super.onStop(owner);
        }

        @Override
        public void onDestroy(@NonNull FlowlyOwner owner) {
            DefaultFlowlyObserver.super.onDestroy(owner);
        }
    };

    private static HashMap<String, WeakReference<FlowlyOwner>> mFlowlyOwners = new HashMap<>();
    protected ParseFlowlyOwner(AppCompatActivity compatActivity) {
        Lifecycle lifecycle = ((LifecycleOwner) compatActivity).getLifecycle();
        lifecycle.addObserver(new DefaultLifecycleObserver() {
            @Override
            public void onCreate(@NonNull LifecycleOwner owner) {
                currentState = Flowly.State.CREATED;
//                logger.d(name + " 2onCreate");
            }

            @Override
            public void onStart(@NonNull LifecycleOwner owner) {
                currentState = Flowly.State.STARTED;
//                logger.d(name + " onStart");
            }

            @Override
            public void onResume(@NonNull LifecycleOwner owner) {
                currentState = Flowly.State.RESUMED;
//                logger.d(name + " onResume");
            }

            @Override
            public void onPause(@NonNull LifecycleOwner owner) {
                currentState = Flowly.State.STARTED;
//                logger.d(name + " onPause");
            }

            @Override
            public void onStop(@NonNull LifecycleOwner owner) {
                currentState = Flowly.State.CREATED;
//                logger.d(name + " onStop");
            }

            @Override
            public void onDestroy(@NonNull LifecycleOwner owner) {
                currentState = Flowly.State.DESTROYED;
//                logger.d(name + " onDestroy");
            }
        });
    }
}
