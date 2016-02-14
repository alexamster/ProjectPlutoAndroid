package com.projectpluto.projectplutoandroid.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;

import com.projectpluto.projectplutoandroid.core.BusProvider;
import com.squareup.otto.Bus;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

/**
 * BleScanner is responsible for starting/stopping Bluetooth Low Energy scans. It will post events
 * to the bus when results/errors are received.
 */
public class BleScanner {
    protected static final int BLE_SCAN_DURATION_MS = 5 * 1000; // 5 seconds
    protected Bus mBus = BusProvider.getInstance();
    protected BluetoothAdapter mBluetoothAdapter;
    protected BluetoothLeScanner mBleScanner;
    protected Handler mHandler = new Handler();
    protected Set<ScanResult> mScanResultSet = new HashSet<>();

    public BleScanner(Context context) {
        BluetoothManager bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBleScanner = mBluetoothAdapter.getBluetoothLeScanner();
    }

    /**
     * Callback that we hand off to Androids BluetoothLeScanner so that it can notify us of scan
     * results.
     */
    protected ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Timber.d("Received scan result for device: %s", result.getDevice().getName());
            mScanResultSet.add(result);
            mBus.post(new ScanResultEvent(mScanResultSet));
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            // we are not using batch results
            throw new IllegalStateException("Unexpected batch result");
        }

        @Override
        public void onScanFailed(int errorCode) {
            Timber.e("Bluetooth scan failed with error code: %d", errorCode);
            mBus.post(new ScanFailureEvent(errorCode));
        }
    };

    protected Runnable mStopScanAction = new Runnable() {
        @Override
        public void run() {
            mBleScanner.stopScan(mScanCallback);
        }
    };

    /**
     * Starts a BLE scan for devices that will last BLE_SCAN_DURATION_MS
     *
     * If a scan is already active then calling this well cancel the old scan
     * and clear its results.
     */
    public void scanForBleDevices() {
        Timber.d("Scanning for BLE devices");
        stopBleScan();

        mBleScanner.startScan(mScanCallback);
        mHandler.postDelayed(mStopScanAction, BLE_SCAN_DURATION_MS);
    }


    /**
     * Stops a BLE scan if one is running and removes any pending stop scan actions
     */
    public void stopBleScan() {
        mHandler.removeCallbacks(mStopScanAction);
        mBleScanner.stopScan(mScanCallback);
        mScanResultSet.clear();
    }

    /**
     * results = All ScanResults that have been found so far in the current scan.
     */
    public static class ScanResultEvent {
        public final Set<ScanResult> results;

        public ScanResultEvent(Set<ScanResult> results) {
            this.results = results;
        }
    }

    /**
     * errorCode = error code for scan failure
     */
    public static class ScanFailureEvent {
        public final int errorCode;

        public ScanFailureEvent(int errorCode) {
            this.errorCode = errorCode;
        }
    }

}
