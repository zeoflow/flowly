/*
 * Copyright 2022 (C) ZeoFlow SRL
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Class that can receive any flowly change and dispatch it to the receiver.
 * <p>
 * If a class implements both this interface and
 * {@link com.zeoflow.flowly.DefaultLifecycleObserver}, then
 * methods of {@code DefaultLifecycleObserver} will be called first, and then followed by the call
 * of {@link LifecycleEventObserver#onStateChanged(LifecycleOwner, Lifecycle.Event)}
 * <p>
 * If a class implements this interface and in the same time uses {@link OnLifecycleEvent}, then
 * annotations will be ignored.
 */
public interface LifecycleEventObserver extends LifecycleObserver {
    /**
     * Called when a state transition event happens.
     *
     * @param source The source of the event
     * @param event The event
     */
    void onStateChanged(
            @NonNull LifecycleOwner source,
            @NonNull Lifecycle.Event event
    );
    /**
     * Called when a state transition event happens.
     *
     * @param source The source of the event
     * @param event The event
     * @param args The arguments
     */
    void onStateChanged(
            @NonNull LifecycleOwner source,
            @NonNull Lifecycle.Event event,
            @Nullable Object... args
    );
}
