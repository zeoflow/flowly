package com.zeoflow.flowly;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Internal class to handle flowly conversion etc.
 *
 * @hide
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
public class Flowlying {

    private static final int REFLECTIVE_CALLBACK = 1;
    private static final int GENERATED_CALLBACK = 2;

    private static final Map<Class<?>, Integer> sCallbackCache = new HashMap<>();
    private static final Map<Class<?>, List<Constructor<? extends GeneratedAdapter>>> sClassToAdapters =
            new HashMap<>();

    // Left for binary compatibility when flowly-common goes up 2.1 as transitive dep
    // but flowly-runtime stays 2.0

    /**
     * @deprecated Left for compatibility with flowly-runtime:2.0
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    @NonNull
    static GenericFlowlyObserver getCallback(final Object object) {
        final FlowlyEventObserver observer = lifecycleEventObserver(object);
        return new GenericFlowlyObserver() {
            @Override
            public void onStateChanged(
                    @NonNull FlowlyOwner source,
                    @NonNull Flowly.Event event
            ) {
                observer.onStateChanged(source, event);
            }

            @Override
            public void onStateChanged(
                    @NonNull FlowlyOwner source,
                    @NonNull Flowly.Event event,
                    @Nullable Object... args
            ) {
                observer.onStateChanged(source, event);
            }
        };
    }

    @NonNull
    static FlowlyEventObserver lifecycleEventObserver(Object object) {
        boolean isLifecycleEventObserver = object instanceof FlowlyEventObserver;
        boolean isFullLifecycleObserver = object instanceof FullFlowlyObserver;
        if (isLifecycleEventObserver && isFullLifecycleObserver) {
            return new FullFlowlyObserverAdapter((FullFlowlyObserver) object,
                    (FlowlyEventObserver) object);
        }
        if (isFullLifecycleObserver) {
            return new FullFlowlyObserverAdapter((FullFlowlyObserver) object, null);
        }

        if (isLifecycleEventObserver) {
            return (FlowlyEventObserver) object;
        }

        final Class<?> klass = object.getClass();
        int type = getObserverConstructorType(klass);
        if (type == GENERATED_CALLBACK) {
            List<Constructor<? extends GeneratedAdapter>> constructors =
                    sClassToAdapters.get(klass);
            if (constructors.size() == 1) {
                GeneratedAdapter generatedAdapter = createGeneratedAdapter(
                        constructors.get(0), object);
                return new SingleGeneratedAdapterObserver(generatedAdapter);
            }
            GeneratedAdapter[] adapters = new GeneratedAdapter[constructors.size()];
            for (int i = 0; i < constructors.size(); i++) {
                adapters[i] = createGeneratedAdapter(constructors.get(i), object);
            }
            return new CompositeGeneratedAdaptersObserver(adapters);
        }
        return new ReflectiveGenericFlowlyObserver(object);
    }

    private static GeneratedAdapter createGeneratedAdapter(
            Constructor<? extends GeneratedAdapter> constructor, Object object) {
        //noinspection TryWithIdenticalCatches
        try {
            return constructor.newInstance(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("deprecation")
    @Nullable
    private static Constructor<? extends GeneratedAdapter> generatedConstructor(Class<?> klass) {
        try {
            Package aPackage = klass.getPackage();
            String name = klass.getCanonicalName();
            final String fullPackage = aPackage != null ? aPackage.getName() : "";
            final String adapterName = getAdapterName(fullPackage.isEmpty() ? name :
                    name.substring(fullPackage.length() + 1));

            @SuppressWarnings("unchecked") final Class<? extends GeneratedAdapter> aClass =
                    (Class<? extends GeneratedAdapter>) Class.forName(
                            fullPackage.isEmpty() ? adapterName : fullPackage + "." + adapterName);
            Constructor<? extends GeneratedAdapter> constructor =
                    aClass.getDeclaredConstructor(klass);
            if (!constructor.isAccessible()) {
                constructor.setAccessible(true);
            }
            return constructor;
        } catch (ClassNotFoundException e) {
            return null;
        } catch (NoSuchMethodException e) {
            // this should not happen
            throw new RuntimeException(e);
        }
    }

    private static int getObserverConstructorType(Class<?> klass) {
        Integer callbackCache = sCallbackCache.get(klass);
        if (callbackCache != null) {
            return callbackCache;
        }
        int type = resolveObserverCallbackType(klass);
        sCallbackCache.put(klass, type);
        return type;
    }

    private static int resolveObserverCallbackType(Class<?> klass) {
        // anonymous class bug:35073837
        if (klass.getCanonicalName() == null) {
            return REFLECTIVE_CALLBACK;
        }

        Constructor<? extends GeneratedAdapter> constructor = generatedConstructor(klass);
        if (constructor != null) {
            sClassToAdapters.put(klass, Collections.singletonList(constructor));
            return GENERATED_CALLBACK;
        }

        boolean hasLifecycleMethods = ClassesInfoCache.sInstance.hasFlowlyMethods(klass);
        if (hasLifecycleMethods) {
            return REFLECTIVE_CALLBACK;
        }

        Class<?> superclass = klass.getSuperclass();
        List<Constructor<? extends GeneratedAdapter>> adapterConstructors = null;
        if (isLifecycleParent(superclass)) {
            if (getObserverConstructorType(superclass) == REFLECTIVE_CALLBACK) {
                return REFLECTIVE_CALLBACK;
            }
            adapterConstructors = new ArrayList<>(sClassToAdapters.get(superclass));
        }

        for (Class<?> intrface : klass.getInterfaces()) {
            if (!isLifecycleParent(intrface)) {
                continue;
            }
            if (getObserverConstructorType(intrface) == REFLECTIVE_CALLBACK) {
                return REFLECTIVE_CALLBACK;
            }
            if (adapterConstructors == null) {
                adapterConstructors = new ArrayList<>();
            }
            adapterConstructors.addAll(sClassToAdapters.get(intrface));
        }
        if (adapterConstructors != null) {
            sClassToAdapters.put(klass, adapterConstructors);
            return GENERATED_CALLBACK;
        }

        return REFLECTIVE_CALLBACK;
    }

    private static boolean isLifecycleParent(Class<?> klass) {
        return klass != null && FlowlyObserver.class.isAssignableFrom(klass);
    }

    /**
     * Create a name for an adapter class.
     */
    public static String getAdapterName(String className) {
        return className.replace(".", "_") + "_LifecycleManager";
    }

    private Flowlying() {
    }
}
