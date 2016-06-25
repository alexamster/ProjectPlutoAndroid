package com.projectpluto.projectplutoandroid.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;

import com.projectpluto.projectplutoandroid.BuildConfig;
import com.projectpluto.projectplutoandroid.models.PlutoColor;
import com.squareup.otto.Bus;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class BluetoothServiceRobolectricTest extends TestCase {
    @Mock BleConnector bleConnector;
    @Mock PlutoCommunicator plutoCommunicator;
    @Mock BleScanner bleScanner;
    @Mock BluetoothDevice device;
    @Mock BleResultHandler bleResultHandler;
    @Mock Bus bus;

    BluetoothService btService;

    @Before
    public void setUp() {
        btService = Robolectric.buildService(BluetoothService.class).get();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testOnBind() {
        btService.mBinder = mock(BluetoothService.BluetoothServiceBinder.class);
        assertEquals(btService.mBinder, btService.onBind(new Intent()));
    }

    @Test
    public void testOnCreate() {
        btService.mBus = bus;
        btService.onCreate();

        verify(bus, times(1)).register(btService.mBleConnector);
        verify(bus, times(1)).register(btService.mPluto);
    }

    @Test
    public void testStartScan() {
        btService.mBleScanner = bleScanner;
        btService.scanForBleDevices();

        verify(bleScanner, times(1)).scanForBleDevices();
    }

    @Test
    public void testOnDestroy() {
        btService.mBus = bus;
        btService.onDestroy();

        verify(bus, times(1)).unregister(btService.mBleConnector);
        verify(bus, times(1)).unregister(btService.mPluto);
    }

    @Test
    public void testStopScan() {
        btService.mBleScanner = bleScanner;
        btService.stopBleScan();

        verify(bleScanner, times(1)).stopBleScan();
    }

    @Test
    public void testConnect() {
        btService.mBleConnector = bleConnector;
        btService.connect(device, false);

        verify(btService.mBleConnector, times(1)).connect(device, false);
    }

    @Test
    public void testChangeColor() {
        PlutoColor color = new PlutoColor(1, 2, 3);
        btService.mPluto = plutoCommunicator;
        btService.changeColor(color, bleResultHandler);

        verify(btService.mPluto, times(1)).changeColor(color, bleResultHandler);
    }

    @Test
    public void testReadColor() {
        btService.mPluto = mock(PlutoCommunicator.class);
        btService.readColor(bleResultHandler);

        verify(btService.mPluto, times(1)).readColor(bleResultHandler);
    }
}
