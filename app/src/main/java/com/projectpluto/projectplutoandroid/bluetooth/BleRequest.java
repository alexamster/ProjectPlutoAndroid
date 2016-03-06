package com.projectpluto.projectplutoandroid.bluetooth;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.support.annotation.Nullable;

import java.util.Arrays;

/**
 * Wrapper for bluetooth requests. This will be used in an eventqueue because android bluetooth stack
 * can only process one command at a time. (the command must complete before we can send over the next)
 */
public class BleRequest {
    public enum RequestType{
        WRITE,
        READ,
        SUBSCRIBE,
        UNSUBSCRIBE
    }

    public final RequestType type;
    public final BluetoothGatt gatt;
    public final BluetoothGattCharacteristic bleChar;
    @Nullable
    public final byte[] data;
    public final BleResultHandler resultHandler;

    public BleRequest(RequestType type,
                      BluetoothGatt gatt,
                      BluetoothGattCharacteristic bleChar,
                      @Nullable byte[] data,
                      BleResultHandler resultHandler) {
        if (null == type ||
            null == gatt ||
            null == bleChar) {

            throw new IllegalStateException("type, gatt and bleChar cannot be null.");
        }

        this.type = type;
        this.gatt = gatt;
        this.bleChar = bleChar;
        this.data = data;
        this.resultHandler = resultHandler;
    }

    @Override
    public String toString() {
        return "BleRequest{" +
                "type=" + type +
                ", gatt=" + gatt +
                ", characteristic=" + bleChar +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}
