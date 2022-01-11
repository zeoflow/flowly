package com.zeoflow.flowly;


class AppStartUp {

    private static final AppStartUp _this = new AppStartUp();
    private final long startUpTime = System.currentTimeMillis();

    private long loadingTime;

    protected AppStartUp() {

    }

    static void init() {
        // empty constructor
    }

    static void appLoaded() {
        _this.loaded();
    }

    static long getLoadingTime() {
        return _this.loadingTime;
    }

    private void loaded() {
        loadingTime = System.currentTimeMillis() - startUpTime;
    }

}
