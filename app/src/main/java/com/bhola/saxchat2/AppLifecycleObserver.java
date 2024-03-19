package com.bhola.saxchat2;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

public class AppLifecycleObserver implements Application.ActivityLifecycleCallbacks {

    private int activityCount = 0;
    private boolean isAppForeground = false;

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (activityCount == 0) {
            // App entered foreground
            isAppForeground = true;
        }
        activityCount++;
    }

    @Override
    public void onActivityResumed(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
        activityCount--;
        if (activityCount == 0) {
            // App went to background
            isAppForeground = false;
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    public boolean isAppInForeground() {
        return isAppForeground;
    }
}
