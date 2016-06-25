package com.projectpluto.projectplutoandroid.bluetooth;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import com.projectpluto.projectplutoandroid.BuildConfig;
import com.projectpluto.projectplutoandroid.models.PlutoColor;
import com.squareup.otto.Bus;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class PlutoCommunicatorTest extends TestCase {
    PlutoCommunicator communicator;
    @Mock BleCommunicator bleCommunicator;
    @Mock Bus bus;
    @Captor ArgumentCaptor<BleRequest> bleRequestCaptor;
    Exception thrownException;

    @Captor ArgumentCaptor<PlutoCommunicator.ColorUpdateEvent> captor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        communicator = new PlutoCommunicator(bleCommunicator);
        communicator.mBus = bus;
        thrownException = null;
    }

    @Test
    public void testOnBleDevicesChanged() {
        assertEquals(0, communicator.mPlutoGatts.size());

        Set<BluetoothGatt> devices = new HashSet<>();
        devices.add(mock(BluetoothGatt.class));
        devices.add(mock(BluetoothGatt.class));
        communicator.onBleDevicesChanged(new BleConnector.BleDevicesChangedEvent(devices));

        assertEquals(communicator.mPlutoGatts, devices);
    }

    @Test
    public void testOnColorBleRead() {
        BluetoothGattCharacteristic characteristic = mock(BluetoothGattCharacteristic.class);
        byte[] color = getTestColor();

        doReturn(color).when(characteristic).getValue();
        doReturn(PlutoCommunicator.PLUTO_COLOR_CHAR).when(characteristic).getUuid();

        communicator.onBleRead(new BleCommunicator.CharacteristicReadEvent(null, characteristic, 0));

        verify(bus, times(1)).post(captor.capture());
        PlutoCommunicator.ColorUpdateEvent event = captor.getValue();
        assertEquals(color, event.color.getBytes());
        assertEquals(color, communicator.mCurrentColor.getBytes());
    }

    @Test
    public void testOtherCharChanged() {
        byte[] color = getTestColor();
        PlutoColor plutoColor = new PlutoColor(color);
        communicator.mCurrentColor = plutoColor;
        BluetoothGattCharacteristic characteristic = mock(BluetoothGattCharacteristic.class);

        doReturn(UUID.randomUUID()).when(characteristic).getUuid();

        communicator.onBleRead(new BleCommunicator.CharacteristicReadEvent(null, characteristic, 0));

        // make sure current color didn't change and nothing posted to bus
        verifyZeroInteractions(bus);
        assertEquals(communicator.mCurrentColor, plutoColor);
    }

    @Test
    public void testChangeColor() {
        PlutoColor color = new PlutoColor(1, 2, 3);
        BluetoothGatt gatt = mock(BluetoothGatt.class);
        BluetoothGattService service = mock(BluetoothGattService.class);
        BluetoothGattCharacteristic gattChar = mock(BluetoothGattCharacteristic.class);

        doReturn(service).when(gatt).getService(PlutoCommunicator.PLUTO_COLOR_SERVICE);
        doReturn(gattChar).when(service).getCharacteristic(PlutoCommunicator.PLUTO_COLOR_CHAR);
        BleResultHandler handler = mock(BleResultHandler.class);
        communicator.mPlutoGatts = new HashSet<>();
        communicator.mPlutoGatts.add(gatt);

        communicator.changeColor(color, handler);

        verify(bleCommunicator, times(1)).enqueueBleRequest(bleRequestCaptor.capture());
        BleRequest request = bleRequestCaptor.getValue();
        assertEquals(request.type, BleRequest.RequestType.WRITE);
        assertEquals(request.data, color.getBytes());
        assertEquals(request.gatt, gatt);
        assertEquals(request.bleChar, gattChar);
        assertEquals(request.resultHandler, handler);
    }

    @Test
    public void testChangeColorMultipleGatts() {
        PlutoColor color = new PlutoColor(1, 2, 3);
        BluetoothGatt gatt = mock(BluetoothGatt.class);
        BluetoothGattService service = mock(BluetoothGattService.class);
        BluetoothGattCharacteristic gattChar = mock(BluetoothGattCharacteristic.class);

        doReturn(service).when(gatt).getService(PlutoCommunicator.PLUTO_COLOR_SERVICE);
        doReturn(gattChar).when(service).getCharacteristic(PlutoCommunicator.PLUTO_COLOR_CHAR);
        BleResultHandler handler = mock(BleResultHandler.class);

        BluetoothGatt gatt2 = mock(BluetoothGatt.class);
        doReturn(service).when(gatt2).getService(PlutoCommunicator.PLUTO_COLOR_SERVICE);
        BluetoothGatt gatt3 = mock(BluetoothGatt.class);
        doReturn(service).when(gatt3).getService(PlutoCommunicator.PLUTO_COLOR_SERVICE);

        communicator.mPlutoGatts = new HashSet<>();
        communicator.mPlutoGatts.add(gatt);
        communicator.mPlutoGatts.add(gatt2);
        communicator.mPlutoGatts.add(gatt3);

        communicator.changeColor(color, handler);

        verify(bleCommunicator, times(3)).enqueueBleRequest(any(BleRequest.class));
    }

    @Test
    public void testReadColor() {
        BluetoothGatt gatt = mock(BluetoothGatt.class);
        BluetoothGattService service = mock(BluetoothGattService.class);
        BluetoothGattCharacteristic gattChar = mock(BluetoothGattCharacteristic.class);

        doReturn(service).when(gatt).getService(PlutoCommunicator.PLUTO_COLOR_SERVICE);
        doReturn(gattChar).when(service).getCharacteristic(PlutoCommunicator.PLUTO_COLOR_CHAR);
        BleResultHandler handler = mock(BleResultHandler.class);
        communicator.mPlutoGatts = new HashSet<>();
        communicator.mPlutoGatts.add(gatt);

        communicator.readColor(handler);

        verify(bleCommunicator, times(1)).enqueueBleRequest(bleRequestCaptor.capture());
        BleRequest request = bleRequestCaptor.getValue();
        assertEquals(request.type, BleRequest.RequestType.READ);
        assertEquals(request.gatt, gatt);
        assertEquals(request.bleChar, gattChar);
        assertEquals(request.resultHandler, handler);
    }

    @Test
    public void testProduce() {
        PlutoColor color = new PlutoColor(1, 2, 3);
        communicator.mCurrentColor = color;
        PlutoCommunicator.ColorUpdateEvent event = communicator.produceCurrentColor();
        assertEquals(event.color, color);
    }

    public byte[] getTestColor() {
        byte[] color = new byte[4];
        color[0] = PlutoColor.COLOR_COMMAND;
        color[1] = 50;
        return color;
    }
}
