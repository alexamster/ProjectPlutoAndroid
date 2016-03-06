package com.projectpluto.projectplutoandroid.core;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

public class BusProvider {
    // Allow events from any thread since events generated from bluetooth callbacks will not be
    // on the main thread.
    private static final Bus BUS = new Bus(ThreadEnforcer.ANY);

    public static Bus getInstance() {
        return BUS;
    }

    private BusProvider() {
        // prevents different instances.
    }
}
