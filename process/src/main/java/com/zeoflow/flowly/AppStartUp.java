package com.zeoflow.flowly;

import com.zeoflow.flowly.debug.Logger;

class AppStartUp {

    private static final Logger logger = new Logger(
            "AppStartUp"
    );

    private static final AppStartUp _this = new AppStartUp();
    private final long startUpTime = System.currentTimeMillis();

    private long loadingTime;

    protected AppStartUp() {
        logger.d(Constants.INITIALIZED);
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
