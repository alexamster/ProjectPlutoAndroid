package com.projectpluto.projectplutoandroid.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

import com.projectpluto.projectplutoandroid.core.BusProvider;
import com.squareup.otto.Bus;
import com.squareup.otto.Produce;

import java.util.HashSet;
import java.util.Set;

import timber.log.Timber;

/**
 * Class used to connect to BLE devices. It is responsible for dispatching connected and
 * disconnected events
 */
public class BleConnector {

    protected Context mContext;
    protected Bus mBus = BusProvider.getInstance();
    protected Set<BluetoothGatt> mConnectedDevices = new HashSet<>();
    protected IBleConnectionStateListener mConnectionListener = new IBleConnectionStateListener() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    gatt.discoverServices();
                    mConnectedDevices.add(gatt);
                    mBus.post(new BleDevicesChangedEvent(mConnectedDevices));
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    mConnectedDevices.remove(gatt);
                    mBus.post(new BleDevicesChangedEvent(mConnectedDevices));
                    break;
                default:
                    Timber.d("Unhandled status %s", status);
            }
        }
    };
    protected BleCommunicator mBleCommunicator = new BleCommunicator(mConnectionListener, true);

    public BleConnector(Context context) {
        mContext = context;
    }

    @Produce
    public BleDevicesChangedEvent produceCurrentConnections() {
        return new BleDevicesChangedEvent(mConnectedDevices);
    }


    public void connect(BluetoothDevice device, boolean autoConnect) {
        device.connectGatt(mContext, autoConnect, mBleCommunicator);
    }

    public void disconnect(BluetoothGatt gatt) {
        gatt.disconnect();
    }

    public static class BleDevicesChangedEvent {
        public final Set<BluetoothGatt> connectedGatts;

        public BleDevicesChangedEvent(Set<BluetoothGatt> connectedGatts) {
            this.connectedGatts = connectedGatts;
        }
    }
}
