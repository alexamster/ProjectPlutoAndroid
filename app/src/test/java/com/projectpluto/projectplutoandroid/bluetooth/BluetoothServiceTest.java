package com.projectpluto.projectplutoandroid.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;

import com.projectpluto.projectplutoandroid.core.Permissions;
import com.projectpluto.projectplutoandroid.models.PlutoColor;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Permissions.class)
public class BluetoothServiceTest extends TestCase {
    @Mock BluetoothService btService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testOnBind() {
        doCallRealMethod().when(btService).onBind(any(Intent.class));
        btService.mBinder = mock(BluetoothService.BluetoothServiceBinder.class);
        assertEquals(btService.mBinder, btService.onBind(new Intent()));
    }

    @Test
    public void testStartScan() {
        doCallRealMethod().when(btService).scanForBleDevices();
        BleScanner scanner = mock(BleScanner.class);
        btService.mBleScanner = scanner;
        btService.scanForBleDevices();

        verify(scanner, times(1)).scanForBleDevices();
    }

    @Test
    public void testStopScan() {
        doCallRealMethod().when(btService).stopBleScan();
        BleScanner scanner = mock(BleScanner.class);
        btService.mBleScanner = scanner;
        btService.stopBleScan();

        verify(scanner, times(1)).stopBleScan();
    }

    @Test
    public void testStartCommandWithPermission() {
        PowerMockito.mockStatic(Permissions.class);
        PowerMockito.when(Permissions.hasCoarseLocation(any(Context.class))).thenReturn(true);

        BluetoothManager manager = mock(BluetoothManager.class);
        BluetoothAdapter adapter = mock(BluetoothAdapter.class);

        when(btService.getSystemService(Context.BLUETOOTH_SERVICE)).thenReturn(manager);
        when(manager.getAdapter()).thenReturn(adapter);

        doCallRealMethod().when(btService).onStartCommand(null, 0, 0);
        int result = btService.onStartCommand(null, 0, 0);

        assertEquals(btService.mBleScanner.mBluetoothAdapter, adapter);
        assertEquals(Service.START_STICKY, result);
    }

    @Test
    public void testStartCommandNoPermission() {
        PowerMockito.mockStatic(Permissions.class);
        PowerMockito.when(Permissions.hasCoarseLocation(any(Context.class))).thenReturn(false);

        doCallRealMethod().when(btService).onStartCommand(null, 0, 0);
        int result = btService.onStartCommand(null, 0, 0);

        verify(btService, times(1)).stopSelf();
        assertEquals(Service.START_NOT_STICKY, result);
    }

    @Test
    public void testConnect() {
        btService.mBleConnector = mock(BleConnector.class);
        BluetoothDevice device = mock(BluetoothDevice.class);
        doCallRealMethod().when(btService).connect(device, false);
        btService.connect(device, false);

        verify(btService.mBleConnector, times(1)).connect(device, false);
    }

    @Test
    public void testChangeColor() {
        PlutoColor color = new PlutoColor(1, 2, 3);
        BleResultHandler handler = mock(BleResultHandler.class);
        btService.mPluto = mock(PlutoCommunicator.class);
        doCallRealMethod().when(btService).changeColor(color, handler);
        btService.changeColor(color, handler);

        verify(btService.mPluto, times(1)).changeColor(color, handler);
    }

    @Test
    public void testReadColor() {
        BleResultHandler handler = mock(BleResultHandler.class);
        btService.mPluto = mock(PlutoCommunicator.class);
        doCallRealMethod().when(btService).readColor(handler);
        btService.readColor(handler);

        verify(btService.mPluto, times(1)).readColor(handler);
    }
}
