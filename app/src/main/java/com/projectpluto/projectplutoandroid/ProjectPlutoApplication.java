package com.projectpluto.projectplutoandroid;

import android.app.Application;

import timber.log.Timber;

public class ProjectPlutoApplication extends Application {
    // Saved as local var so that we can mock out for testing
    protected boolean IS_DEBUG_BUILD = BuildConfig.DEBUG;

    @Override public void onCreate() {
        super.onCreate();

        if (IS_DEBUG_BUILD) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
