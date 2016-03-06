package com.projectpluto.projectplutoandroid.bluetooth;

import android.bluetooth.BluetoothGatt;

public interface IBleConnectionStateListener {
    void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState);
}
