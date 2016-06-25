package com.projectpluto.projectplutoandroid.bluetooth;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.support.annotation.Nullable;

import com.projectpluto.projectplutoandroid.core.BusProvider;
import com.projectpluto.projectplutoandroid.models.PlutoColor;
import com.squareup.otto.Bus;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import timber.log.Timber;

/**
 * PlutoCommunicator is responsible for listening to changes in the color characteristic and
 * broadcasting updates to the rest of the app in the form of ColorUpdateEvent.
 *
 * It also can write new colors to a Pluto board, and trigger reads if the color has changed.
 */
public class PlutoCommunicator {
    protected static final UUID PLUTO_COLOR_SERVICE = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
    protected static final UUID PLUTO_COLOR_CHAR = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");

    protected BleCommunicator mCommunicator;
    protected Bus mBus = BusProvider.getInstance();
    protected Set<BluetoothGatt> mPlutoGatts = new HashSet<>();

    @Nullable
    protected PlutoColor mCurrentColor;

    public PlutoCommunicator(BleCommunicator communicator) {
        mCommunicator = communicator;
    }

    @Subscribe
    public void onBleRead(BleCommunicator.CharacteristicReadEvent event) {
        if (event.characteristic.getUuid() == PLUTO_COLOR_CHAR) {
            PlutoColor color = new PlutoColor(event.characteristic.getValue());
            mCurrentColor = color;
            mBus.post(new ColorUpdateEvent(color));
        }
    }

    @Subscribe
    public void onBleDevicesChanged(BleConnector.BleDevicesChangedEvent event) {
        mPlutoGatts = event.connectedGatts;
    }

    @Produce
    public ColorUpdateEvent produceCurrentColor() {
        return new ColorUpdateEvent(mCurrentColor);
    }

    public void changeColor(PlutoColor color,
                            BleResultHandler handler) {
        for (BluetoothGatt gatt : mPlutoGatts) {
            BluetoothGattCharacteristic bleChar = getGattChar(gatt);
            if (null == bleChar) {
                // failure will already be logged in getGattChar(...), no need to do it twice
                return;
            }

            mCommunicator.enqueueBleRequest(new BleRequest(BleRequest.RequestType.WRITE,
                    gatt,
                    bleChar,
                    color.getBytes(),
                    handler));
        }
    }

    /**
     * Handler result indicates the status of the read request (it does not contain the result).
     * The color will be broadcast via a ColorUpdateEvent once the characteristic is actually read.
     */
    public void readColor(BleResultHandler handler) {
        for (BluetoothGatt gatt : mPlutoGatts) {
            BluetoothGattCharacteristic bleChar = getGattChar(gatt);
            if (null == bleChar) {
                // failure will already be logged in getGattChar(...), no need to do it twice
                return;
            }

            mCommunicator.enqueueBleRequest(new BleRequest(BleRequest.RequestType.READ,
                    gatt,
                    bleChar,
                    null,
                    handler));

            // only get color from one device as they all will be in sync (or will be as soon as
            // we send a color)
            return;
        }
    }

    public BluetoothGattCharacteristic getGattChar(BluetoothGatt gatt) {
        BluetoothGattService service = gatt.getService(PLUTO_COLOR_SERVICE);
        if (service == null) {
            Timber.e("Unable to find pluto service on device %s", gatt.getDevice().getName());
            return null;
        }

        BluetoothGattCharacteristic bleChar = service.getCharacteristic(PLUTO_COLOR_CHAR);
        if (bleChar == null) {
            Timber.e("Unable to find pluto service on device %s", gatt.getDevice().getName());
            return null;
        }

        return bleChar;
    }

    public static class ColorUpdateEvent {
        @Nullable
        public final PlutoColor color;

        public ColorUpdateEvent(@Nullable PlutoColor color) {
            this.color = color;
        }
    }
}
