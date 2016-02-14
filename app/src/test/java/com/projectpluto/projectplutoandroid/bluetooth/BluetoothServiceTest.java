package com.projectpluto.projectplutoandroid.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;

import com.projectpluto.projectplutoandroid.core.Permissions;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
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

    @Test
    public void testOnBind() throws Exception {
        BluetoothService btService = new BluetoothService();
        assertEquals(btService.mBinder, btService.onBind(new Intent()));
    }

    @Test
    public void testStartScan() throws Exception {
        BluetoothService btService = new BluetoothService();
        BleScanner scanner = mock(BleScanner.class);
        btService.mBleScanner = scanner;
        btService.scanForBleDevices();

        verify(scanner, times(1)).scanForBleDevices();
    }

    @Test
    public void testStopScan() throws Exception {
        BluetoothService btService = new BluetoothService();
        BleScanner scanner = mock(BleScanner.class);
        btService.mBleScanner = scanner;
        btService.stopBleScan();

        verify(scanner, times(1)).stopBleScan();
    }

    @Test
    public void testStartCommandWithPermission() throws Exception {
        PowerMockito.mockStatic(Permissions.class);
        PowerMockito.when(Permissions.hasCoarseLocation(any(Context.class))).thenReturn(true);

        BluetoothService btService = mock(BluetoothService.class);
        BluetoothManager manager = mock(BluetoothManager.class);
        BluetoothAdapter adapter = mock(BluetoothAdapter.class);

        when(btService.getSystemService(Context.BLUETOOTH_SERVICE)).thenReturn(manager);
        when(manager.getAdapter()).thenReturn(adapter);

        doCallRealMethod().when(btService).onStartCommand(null, 0, 0);
        int result = btService.onStartCommand(null, 0, 0);

        assertEquals(Service.START_STICKY, result);
        assertNotNull(btService.mBleScanner);
    }

    @Test
    public void testStartCommandNoPermission() throws Exception {
        PowerMockito.mockStatic(Permissions.class);
        PowerMockito.when(Permissions.hasCoarseLocation(any(Context.class))).thenReturn(false);

        BluetoothService btService = mock(BluetoothService.class);
        doCallRealMethod().when(btService).onStartCommand(null, 0, 0);
        int result = btService.onStartCommand(null, 0, 0);

        verify(btService, times(1)).stopSelf();
        assertEquals(Service.START_NOT_STICKY, result);
    }
}
