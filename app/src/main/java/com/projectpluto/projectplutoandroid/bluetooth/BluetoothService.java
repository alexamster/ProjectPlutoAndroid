package com.projectpluto.projectplutoandroid.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.projectpluto.projectplutoandroid.core.BusProvider;
import com.projectpluto.projectplutoandroid.core.Permissions;
import com.projectpluto.projectplutoandroid.models.PlutoColor;
import com.squareup.otto.Bus;

import timber.log.Timber;

public class BluetoothService extends Service {
    // Member vars are not private/final so that they can be easily mocked for testing.
    protected BluetoothServiceBinder mBinder = new BluetoothServiceBinder();
    protected BleScanner mBleScanner;
    protected BleConnector mBleConnector = new BleConnector(this);
    protected PlutoCommunicator mPluto = new PlutoCommunicator(mBleConnector.mBleCommunicator);
    protected Bus mBus = BusProvider.getInstance();

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
    public void onCreate() {
        super.onCreate();

        mBus.register(mBleConnector);
        mBus.register(mPluto);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.i("onStartCommand() intent: %s flags: %d startId: %d", intent, flags, startId);

        if (!Permissions.hasCoarseLocation(BluetoothService.this)) {
            Timber.e("App does not have permission ACCESS_COARSE_LOCATION, stopping service");
            stopSelf();
            return START_NOT_STICKY;
        }

        mBleScanner = new BleScanner(BluetoothService.this);
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mBus.unregister(mBleConnector);
        mBus.unregister(mPluto);
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

    /**
     * Connects to specified device. Multiple devices may be connected to at the same time
     */
    public void connect(BluetoothDevice device, boolean autoConnect) {
        mBleConnector.connect(device, autoConnect);
    }

    /**
     * Changes color of all connected pluto devices
     */
    public void changeColor(PlutoColor color, BleResultHandler handler) {
        mPluto.changeColor(color, handler);
    }

    /**
     * Read color from pluto device (if connected to multiple, first device will be read)
     */
    public void readColor(BleResultHandler handler) {
        mPluto.readColor(handler);
    }
}
