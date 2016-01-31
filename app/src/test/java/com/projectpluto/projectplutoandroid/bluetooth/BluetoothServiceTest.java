package com.projectpluto.projectplutoandroid.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;

import junit.framework.TestCase;

import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class BluetoothServiceTest extends TestCase {
    @Mock BluetoothManager mBtManager;
    @Mock BluetoothAdapter mBtAdapter;
    @Mock Context mContext;
    @Mock BluetoothLeScanner mAndroidScanner;

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
}
