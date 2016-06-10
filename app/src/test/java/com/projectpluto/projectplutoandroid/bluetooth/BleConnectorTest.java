package com.projectpluto.projectplutoandroid.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

import com.projectpluto.projectplutoandroid.core.BusProvider;
import com.squareup.otto.Bus;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest(BusProvider.class)
public class BleConnectorTest extends TestCase {
    @Mock Bus bus;

    protected void setUp() {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(BusProvider.class);
        PowerMockito.when(BusProvider.getInstance()).thenReturn(bus);
    }

    @Test
    public void testRegisterOnCreate() {
        BleConnector connector = new BleConnector(mock(Context.class));
        verify(bus, times(1)).register(connector);
    }

    @Test
    public void testProduceDevices() {
        BleConnector connector = new BleConnector(mock(Context.class));
        BleConnector.BleDevicesChangedEvent event = connector.produceCurrentConnections();
        assertEquals(event.connectedGatts, connector.mConnectedDevices);
    }

    @Test
    public void testConnect() {
        BleConnector connector = new BleConnector(mock(Context.class));
        BluetoothDevice device = mock(BluetoothDevice.class);
        connector.connect(device, false);
        verify(device, times(1)).connectGatt(Mockito.eq(connector.mContext),
                Mockito.eq(false),
                any(BluetoothGattCallback.class));
    }

    @Test
    public void testDisconnect() {
        BleConnector connector = new BleConnector(mock(Context.class));
        BluetoothGatt gatt = mock(BluetoothGatt.class);
        connector.disconnect(gatt);
        verify(gatt, times(1)).disconnect();
    }

    @Test
    public void testConnectionStateChangeConnected() {
        BleConnector connector = new BleConnector(mock(Context.class));
        BluetoothGatt gatt = mock(BluetoothGatt.class);
        connector.mConnectionListener.onConnectionStateChange(gatt, 0, BluetoothProfile.STATE_CONNECTED);
        assertEquals(connector.mConnectedDevices.size(), 1);
        assertTrue(connector.mConnectedDevices.contains(gatt));

        ArgumentCaptor<BleConnector.BleDevicesChangedEvent> captor =
                ArgumentCaptor.forClass(BleConnector.BleDevicesChangedEvent.class);
        verify(bus).post(captor.capture());
        assertEquals(connector.mConnectedDevices, captor.getValue().connectedGatts);
    }

    @Test
    public void testConnectionStateChangeDisconnected() {
        BleConnector connector = new BleConnector(mock(Context.class));
        BluetoothGatt gatt = mock(BluetoothGatt.class);
        connector.mConnectedDevices.add(gatt);
        connector.mConnectionListener.onConnectionStateChange(gatt, 0, BluetoothProfile.STATE_DISCONNECTED);
        assertEquals(connector.mConnectedDevices.size(), 0);

        ArgumentCaptor<BleConnector.BleDevicesChangedEvent> captor =
                ArgumentCaptor.forClass(BleConnector.BleDevicesChangedEvent.class);
        verify(bus).post(captor.capture());
        assertEquals(connector.mConnectedDevices, captor.getValue().connectedGatts);
    }
}