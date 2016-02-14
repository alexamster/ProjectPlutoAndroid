package com.projectpluto.projectplutoandroid.bluetooth;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.projectpluto.projectplutoandroid.core.Permissions;

import timber.log.Timber;

public class BluetoothService extends Service {
    // Member vars are not private/final so that they can be easily mocked for testing.
    protected BluetoothServiceBinder mBinder = new BluetoothServiceBinder();
    protected BleScanner mBleScanner;

    /**
     * Class for clients to access. Because we know this service always runs in
     * the same process as its clients, we don't need to deal with IPC.
     */
    public class BluetoothServiceBinder extends Binder {
        public BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.i("onStartCommand() intent: " + intent + " flags: " + flags + " startId: " + startId);

        if (!Permissions.hasCoarseLocation(BluetoothService.this)) {
            Timber.e("App does not have permission ACCESS_COARSE_LOCATION, stopping service");
            stopSelf();
            return START_NOT_STICKY;
        }

        mBleScanner = new BleScanner(BluetoothService.this);
        return Service.START_STICKY;
    }

    /**
     * Starts a BLE scan for devices that will last BLE_SCAN_DURATION_MS
     *
     * If a scan is already active then calling this well cancel the old scan,
     */
    public void scanForBleDevices() {
        mBleScanner.scanForBleDevices();
    }

    /**
     * Stops a BLE scan if one is running.
     */
    public void stopBleScan() {
        mBleScanner.stopBleScan();
    }
}
