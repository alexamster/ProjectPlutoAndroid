package com.projectpluto.projectplutoandroid.bluetooth;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import com.projectpluto.projectplutoandroid.core.BusProvider;
import com.squareup.otto.Bus;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import timber.log.Timber;

public class BleCommunicator extends BluetoothGattCallback {
    // Client Characteristic Configuration descriptor UUID
    // https://developer.bluetooth.org/gatt/descriptors/Pages/DescriptorViewer.aspx?u=org.bluetooth.descriptor.gatt.client_characteristic_configuration.xml
    protected static UUID CLIENT_CONFIG_DESCRIPTOR_ID = UUID.fromString(
            "00002902-0000-1000-8000-00805f9b34fb");

    protected Bus mBus = BusProvider.getInstance();
    protected IBleConnectionStateListener mConnectionStateListener;
    protected BlockingQueue<BleRequest> mRequestQueue = new LinkedBlockingQueue<>();
    protected Thread mProcessQueueThread = new Thread(new Runnable() {
        @Override
        public void run() {
            processNext();
        }
    });

    public BleCommunicator(IBleConnectionStateListener connectionStateListener) {
        mConnectionStateListener = connectionStateListener;
        mProcessQueueThread.start();
    }

    public void enqueueBleRequest(BleRequest request) {
        mRequestQueue.add(request);
    }

    /**
     * Android bluetooth commands such as writeCharacteristic() are non-blocking, but if another
     * command is run before the first command finishes it will fail. This forces us to implement
     * a queue for bluetooth commands and only execute them after the previous command finishes.
     *
     * This will take the next command from the queue (blocking if empty) and executes the request.
     * If the command succeeds, processNext() will be invoked from the corresponding BLE callback
     * (such as onCharacteristicRead(...)).
     *
     * If the command fails, processNext() will be called again immediately.
     *
     * This method should only be called from mProcessQueueThread during class creation, from itself
     * or from the BLE callbacks.
     */
    protected void processNext() {
        BleRequest request;
        try {
            request = mRequestQueue.take();
        } catch (InterruptedException e) {
            Timber.e(e, "Ble queue interrupted");
            return;
        }

        boolean result;
        switch (request.type) {
            case WRITE:
                request.bleChar.setValue(request.data);
                result = request.gatt.writeCharacteristic(request.bleChar);
                break;

            case READ:
                result = request.gatt.readCharacteristic(request.bleChar);
                break;

            case SUBSCRIBE:
                BluetoothGattDescriptor subDescriptor
                        = request.bleChar.getDescriptor(CLIENT_CONFIG_DESCRIPTOR_ID);
                result = subDescriptor.setValue(
                        BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                if (result) {
                    result = request.gatt.writeDescriptor(subDescriptor);
                } else {
                    Timber.e("Unable to set subscription descriptor value");
                }
                break;

            case UNSUBSCRIBE:
                BluetoothGattDescriptor unsubDescriptor
                        = request.bleChar.getDescriptor(CLIENT_CONFIG_DESCRIPTOR_ID);
                result = unsubDescriptor.setValue(
                        BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                if (result) {
                    result = request.gatt.writeDescriptor(unsubDescriptor);
                } else {
                    Timber.e("Unable to set unsubscribe descriptor value");
                }
                break;
            default:
                throw new IllegalStateException("Unknown request type");
        }

        if (result) {
            request.resultHandler.onSuccess();
        } else {
            request.resultHandler.onError();
            processNext();
        }
    }

    @Override public void onConnectionStateChange(final BluetoothGatt gatt,
                                                  int status,
                                                  int newState) {
        super.onConnectionStateChange(gatt, status, newState);
        Timber.d("GattCallBack.onConnectionStateChange status=%s newState=%s", status, newState);
        mConnectionStateListener.onConnectionStateChange(gatt, status, newState);
    }

    @Override public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
        Timber.d("onServicesDiscovered: %s status=%s: ",
                gatt.getDevice().getAddress(),
                status);

        mBus.post(new ServiceDiscoveredEvent(gatt, status));
    }

    @Override public void onDescriptorWrite(BluetoothGatt gatt,
                                            BluetoothGattDescriptor descriptor,
                                            int status) {
        super.onDescriptorWrite(gatt, descriptor, status);
        Timber.d("onDescriptorWrite: device=%s descriptor=%s status=%d",
                gatt.getDevice().getAddress(),
                descriptor.toString(),
                status);

        mBus.post(new DescriptorWriteEvent(gatt, descriptor, status));
        processNext();
    }

    @Override public void onCharacteristicChanged(BluetoothGatt gatt,
                                                  BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);
        mBus.post(new CharacteristicChangedEvent(gatt, characteristic));
        processNext();
    }

    @Override public void onCharacteristicRead(BluetoothGatt gatt,
                                               BluetoothGattCharacteristic characteristic,
                                               int status) {
        super.onCharacteristicRead(gatt, characteristic, status);
        mBus.post(new CharacteristicReadEvent(gatt, characteristic, status));
        processNext();
    }

    @Override public void onCharacteristicWrite(BluetoothGatt gatt,
                                                BluetoothGattCharacteristic characteristic,
                                                int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
        mBus.post(new CharacteristicWriteEvent(gatt, characteristic, status));
        processNext();
    }

    public static class ServiceDiscoveredEvent {
        public final BluetoothGatt gatt;
        public final int status;

        public ServiceDiscoveredEvent(BluetoothGatt gatt, int status) {
            this.gatt = gatt;
            this.status = status;
        }
    }

    public static class DescriptorWriteEvent {
        public final BluetoothGatt gatt;
        public final BluetoothGattDescriptor descriptor;
        public final int status;

        public DescriptorWriteEvent(BluetoothGatt gatt,
                                    BluetoothGattDescriptor descriptor,
                                    int status) {
            this.gatt = gatt;
            this.descriptor = descriptor;
            this.status = status;
        }
    }

    public static class CharacteristicChangedEvent {
        public final BluetoothGatt gatt;
        public final BluetoothGattCharacteristic characteristic;

        public CharacteristicChangedEvent(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic) {
            this.gatt = gatt;
            this.characteristic = characteristic;
        }
    }

    public static class CharacteristicReadEvent {
        public final BluetoothGatt gatt;
        public final BluetoothGattCharacteristic characteristic;
        public final int status;

        public CharacteristicReadEvent(BluetoothGatt gatt,
                                       BluetoothGattCharacteristic characteristic,
                                       int status) {
            this.gatt = gatt;
            this.characteristic = characteristic;
            this.status = status;
        }
    }

    public static class CharacteristicWriteEvent {
        public final BluetoothGatt gatt;
        public final BluetoothGattCharacteristic characteristic;
        public final int status;

        public CharacteristicWriteEvent(BluetoothGatt gatt,
                                       BluetoothGattCharacteristic characteristic,
                                       int status) {
            this.gatt = gatt;
            this.characteristic = characteristic;
            this.status = status;
        }
    }
}
