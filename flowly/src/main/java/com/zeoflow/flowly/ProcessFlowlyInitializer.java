/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zeoflow.flowly;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ProcessLifecycleInitializer;

import com.zeoflow.startup.Initializer;

import java.util.Collections;
import java.util.List;

/**
 * Initializes {@link ProcessLifecycleInitializer} using {@code com.zeoflow.startup}.
 */
public final class ProcessFlowlyInitializer implements Initializer<ApplicationManager> {

    public ProcessFlowlyInitializer() {

    }

    @NonNull
    @Override
    public ApplicationManager create(@NonNull Context context) {
        return ApplicationManager.init((Application) context.getApplicationContext());
    }

    @NonNull
    @Override
    public List<Class<? extends Initializer<?>>> dependencies() {
        return Collections.emptyList();
    }
}
