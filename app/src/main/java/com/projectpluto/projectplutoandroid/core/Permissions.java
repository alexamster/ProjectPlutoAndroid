package com.projectpluto.projectplutoandroid.core;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

public class Permissions {
    /**
     * Checks if application has permission ACCESS_COARSE_LOCATION, which is required for bluetooth
     * scan.
     */
    public static boolean hasCoarseLocation(Context con) {
        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(con,
                Manifest.permission.ACCESS_COARSE_LOCATION)) {
            return false;
        }
        return true;
    }
}
