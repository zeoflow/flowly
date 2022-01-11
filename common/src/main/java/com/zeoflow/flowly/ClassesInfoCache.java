package com.zeoflow.flowly;

import androidx.annotation.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reflection is expensive, so we cache information about methods
 * for {@link ReflectiveGenericFlowlyObserver}, so it can call them,
 * and for {@link Flowlying} to determine which observer adapter to use.
 */
final class ClassesInfoCache {

    static ClassesInfoCache sInstance = new ClassesInfoCache();

    private static final int CALL_TYPE_NO_ARG = 0;
    private static final int CALL_TYPE_PROVIDER = 1;
    private static final int CALL_TYPE_PROVIDER_WITH_EVENT = 2;
    private static final int CALL_TYPE_LONG = 3;

    private final Map<Class<?>, CallbackInfo> mCallbackMap = new HashMap<>();
    private final Map<Class<?>, Boolean> mHasFlowlyMethods = new HashMap<>();

    boolean hasFlowlyMethods(Class<?> klass) {

        Boolean hasFlowlyMethods = mHasFlowlyMethods.get(klass);
        if (hasFlowlyMethods != null) {
            return hasFlowlyMethods;
        }

        Method[] methods = getDeclaredMethods(klass);
        for (Method method : methods) {
            OnFlowlyEvent annotation = method.getAnnotation(OnFlowlyEvent.class);
            if (annotation != null) {
                // Optimization for reflection, we know that this method is called
                // when there is no generated adapter. But there are methods with @OnFlowlyEvent
                // so we know that will use ReflectiveGenericFlowlyObserver,
                // so we createInfo in advance.
                // CreateInfo always initialize mHasFlowlyMethods for a class, so we don't do it
                // here.
                createInfo(klass, methods);
                return true;
            }
        }
        mHasFlowlyMethods.put(klass, false);
        return false;
    }

    private Method[] getDeclaredMethods(Class<?> klass) {
        try {
            return klass.getDeclaredMethods();
        } catch (NoClassDefFoundError e) {
            throw new IllegalArgumentException("The observer class has some methods that use "
                    + "newer APIs which are not available in the current OS version. Flowlys "
                    + "cannot access even other methods so you should make sure that your "
                    + "observer classes only access framework classes that are available "
                    + "in your min API level OR use flowly:compiler annotation processor.", e);
        }
    }

    CallbackInfo getInfo(Class<?> klass) {
        CallbackInfo existing = mCallbackMap.get(klass);
        if (existing != null) {
            return existing;
        }
        existing = createInfo(klass, null);
        return existing;
    }

    private void verifyAndPutHandler(Map<MethodReference, Flowly.Event> handlers,
                                     MethodReference newHandler, Flowly.Event newEvent, Class<?> klass) {
        Flowly.Event event = handlers.get(newHandler);
        if (event != null && newEvent != event) {
            Method method = newHandler.mMethod;
            throw new IllegalArgumentException(
                    "Method " + method.getName() + " in " + klass.getName()
                            + " already declared with different @OnFlowlyEvent value: previous"
                            + " value " + event + ", new value " + newEvent);
        }
        if (event == null) {
            handlers.put(newHandler, newEvent);
        }
    }

    private CallbackInfo createInfo(Class<?> klass, @Nullable Method[] declaredMethods) {
        Class<?> superclass = klass.getSuperclass();
        Map<MethodReference, Flowly.Event> handlerToEvent = new HashMap<>();
        if (superclass != null) {
            CallbackInfo superInfo = getInfo(superclass);
            handlerToEvent.putAll(superInfo.mHandlerToEvent);
        }

        Class<?>[] interfaces = klass.getInterfaces();
        for (Class<?> intrfc : interfaces) {
            for (Map.Entry<MethodReference, Flowly.Event> entry : getInfo(
                    intrfc).mHandlerToEvent.entrySet()) {
                verifyAndPutHandler(handlerToEvent, entry.getKey(), entry.getValue(), klass);
            }
        }

        Method[] methods = declaredMethods != null ? declaredMethods : getDeclaredMethods(klass);
        boolean hasFlowlyMethods = false;
        for (Method method : methods) {
            OnFlowlyEvent annotation = method.getAnnotation(OnFlowlyEvent.class);
            if (annotation == null) {
                continue;
            }
            hasFlowlyMethods = true;
            Class<?>[] params = method.getParameterTypes();
            int callType = CALL_TYPE_NO_ARG;
            // TODO assign different call types
            if (params.length > 0) {
                if (params[0].isAssignableFrom(FlowlyOwner.class)) {
                    callType = CALL_TYPE_PROVIDER;
                }
//                if (!params[0].isAssignableFrom(FlowlyOwner.class)) {
//                    throw new IllegalArgumentException(
//                            "invalid parameter type. Must be one and instanceof FlowlyOwner");
//                }
            }
            Flowly.Event event = annotation.value();

            if (params.length > 1) {
                if (params[1].isAssignableFrom(Flowly.Event.class)) {
                    callType = CALL_TYPE_PROVIDER_WITH_EVENT;
                }
//                if (!params[1].isAssignableFrom(Flowly.Event.class)) {
//                    throw new IllegalArgumentException(
//                            "invalid parameter type. second arg must be an event");
//                }
//                if (event != Flowly.Event.ON_ANY) {
//                    throw new IllegalArgumentException(
//                            "Second arg is supported only for ON_ANY value");
//                }
            }
//            if (params.length > 2) {
//                throw new IllegalArgumentException("cannot have more than 2 params");
//            }

            if (params.length == 1 && callType == CALL_TYPE_NO_ARG) {
                if (params[0].isAssignableFrom(long.class)) {
                    callType = CALL_TYPE_LONG;
                }
            }
            MethodReference methodReference = new MethodReference(callType, method);
            verifyAndPutHandler(handlerToEvent, methodReference, event, klass);
        }
        CallbackInfo info = new CallbackInfo(handlerToEvent);
        mCallbackMap.put(klass, info);
        mHasFlowlyMethods.put(klass, hasFlowlyMethods);
        return info;
    }

    @SuppressWarnings("WeakerAccess")
    static class CallbackInfo {
        final Map<Flowly.Event, List<MethodReference>> mEventToHandlers;
        final Map<MethodReference, Flowly.Event> mHandlerToEvent;

        CallbackInfo(Map<MethodReference, Flowly.Event> handlerToEvent) {
            mHandlerToEvent = handlerToEvent;
            mEventToHandlers = new HashMap<>();
            for (Map.Entry<MethodReference, Flowly.Event> entry : handlerToEvent.entrySet()) {
                Flowly.Event event = entry.getValue();
                List<MethodReference> methodReferences = mEventToHandlers.get(event);
                if (methodReferences == null) {
                    methodReferences = new ArrayList<>();
                    mEventToHandlers.put(event, methodReferences);
                }
                methodReferences.add(entry.getKey());
            }
        }

        void invokeCallbacks(FlowlyOwner source, Flowly.Event event, Object target, Object... args) {
            invokeMethodsForEvent(
                    mEventToHandlers.get(event),
                    source,
                    event,
                    target,
                    args
            );
            invokeMethodsForEvent(
                    mEventToHandlers.get(Flowly.Event.ON_ANY),
                    source,
                    event,
                    target,
                    args
            );
        }

        private static void invokeMethodsForEvent(List<MethodReference> handlers,
                                                  FlowlyOwner source, Flowly.Event event, Object mWrapped, Object... args) {
            if (handlers != null) {
                for (int i = handlers.size() - 1; i >= 0; i--) {
                    handlers.get(i).invokeCallback(source, event, mWrapped, args);
                }
            }
        }
    }

    @SuppressWarnings("WeakerAccess")
    static final class MethodReference {
        final int mCallType;
        final Method mMethod;

        MethodReference(int callType, Method method) {
            mCallType = callType;
            mMethod = method;
            mMethod.setAccessible(true);
        }

        void invokeCallback(FlowlyOwner source, Flowly.Event event, Object target, Object... args) {
            try {
                if (event.isActivityEvent()) {
                    if (mCallType == CALL_TYPE_NO_ARG) {
                        mMethod.invoke(target);
                    } else {
                        mMethod.invoke(target, args);
                    }
                    return;
                } else if (event.isApplicationEvent()) {
                    if (mCallType == CALL_TYPE_NO_ARG) {
                        mMethod.invoke(target);
                    } else {
                        mMethod.invoke(target, args);
                    }
                    return;
                }
                switch (mCallType) {
                    case CALL_TYPE_NO_ARG:
                        mMethod.invoke(target);
                        break;
                    case CALL_TYPE_PROVIDER:
                        mMethod.invoke(target, source);
                        break;
                    case CALL_TYPE_PROVIDER_WITH_EVENT:
                        mMethod.invoke(target, source, event);
                        break;
                }
            } catch (InvocationTargetException e) {
//                throw new RuntimeException("Failed to call observer method", e.getCause());
            } catch (IllegalAccessException e) {
//                throw new RuntimeException(e);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof MethodReference)) {
                return false;
            }

            MethodReference that = (MethodReference) o;
            return mCallType == that.mCallType && mMethod.getName().equals(that.mMethod.getName());
        }

        @Override
        public int hashCode() {
            return 31 * mCallType + mMethod.getName().hashCode();
        }
    }
}
