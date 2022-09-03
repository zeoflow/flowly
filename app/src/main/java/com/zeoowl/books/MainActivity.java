package com.zeoowl.books;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.zeoflow.flowly.ApplicationManager;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "getFlowlyOwner : " + (ApplicationManager.getLifecycleOwner() == null));
        ApplicationManager.getLifecycleOwner().getLifecycle().addObserver(new DefaultLifecycleObserver() {
            @Override
            public void onCreate(@NonNull LifecycleOwner owner) {
                Log.d(TAG, "onCreate");
            }

            @Override
            public void onStart(@NonNull LifecycleOwner owner) {
                Log.d(TAG, "onStart");
            }

            @Override
            public void onResume(@NonNull LifecycleOwner owner) {
                Log.d(TAG, "onResume");
            }

            @Override
            public void onPause(@NonNull LifecycleOwner owner) {
                Log.d(TAG, "onPause");
            }

            @Override
            public void onStop(@NonNull LifecycleOwner owner) {
                Log.d(TAG, "onStop");
            }

            @Override
            public void onDestroy(@NonNull LifecycleOwner owner) {
                Log.d(TAG, "onDestroy");
            }
        });
    }
}