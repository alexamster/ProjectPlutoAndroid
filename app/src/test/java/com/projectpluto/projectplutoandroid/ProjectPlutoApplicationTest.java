package com.projectpluto.projectplutoandroid;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import timber.log.Timber;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class ProjectPlutoApplicationTest extends TestCase {

    @Test
    public void testLoggerDebug() throws Exception {
        ProjectPlutoApplication app = new ProjectPlutoApplication();
        Timber.uprootAll();

        app.IS_DEBUG_BUILD = true;
        app.onCreate();
        assertTrue(Timber.forest().size() == 1);
    }

    @Test
    public void testLoggerProduction() throws Exception {
        ProjectPlutoApplication app = new ProjectPlutoApplication();
        Timber.uprootAll();

        app.IS_DEBUG_BUILD = false;
        app.onCreate();
        assertTrue(Timber.forest().size() == 0);
    }
}
