package com.projectpluto.projectplutoandroid.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;

import com.squareup.otto.Bus;

import junit.framework.TestCase;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BleScannerTest extends TestCase {
    @Mock BleScanner mScanner;
    @Mock BluetoothManager mBtManager;
    @Mock BluetoothAdapter mBtAdapter;
    @Mock Context mContext;
    @Mock BluetoothLeScanner mAndroidScanner;
    @Mock Bus mBus;
    @Mock Handler mHandler;

    protected void setUp() {
        MockitoAnnotations.initMocks(this);
        // mock out the bluetooth services, otherwise we will get NPE in BleScanner constructor
        when(mContext.getSystemService(Context.BLUETOOTH_SERVICE)).thenReturn(mBtManager);
        when(mBtManager.getAdapter()).thenReturn(mBtAdapter);
        when(mBtAdapter.getBluetoothLeScanner()).thenReturn(mAndroidScanner);

        mScanner = new BleScanner(mContext);
        mScanner.mBus = this.mBus;
        mScanner.mHandler = this.mHandler;
    }

    @Test
    public void testScanResult() {
        ArgumentCaptor<BleScanner.ScanResultEvent> event
                = ArgumentCaptor.forClass(BleScanner.ScanResultEvent.class);

        ScanResult result = mock(ScanResult.class);
        BluetoothDevice device = mock(BluetoothDevice.class);
        when(result.getDevice()).thenReturn(device);
        when(device.getAddress()).thenReturn("EB:22:19:0E:B2:01");
        mScanner.mScanCallback.onScanResult(1, result);

        verify(mBus, times(1)).post(event.capture());
        assertEquals(event.getValue().addressToResult.get("EB:22:19:0E:B2:01"), result);
    }

    @Test
    public void testScanError() {
        ArgumentCaptor<BleScanner.ScanFailureEvent> event
                = ArgumentCaptor.forClass(BleScanner.ScanFailureEvent.class);

        int errorCode = 5;
        mScanner.mScanCallback.onScanFailed(errorCode);

        verify(mBus, times(1)).post(event.capture());
        assertEquals(errorCode, event.getValue().errorCode);
    }

    @Test
    public void testStopScanAction() {
        mScanner.mStopScanAction.run();

        verify(mAndroidScanner, times(1)).stopScan(mScanner.mScanCallback);
    }

    @Test
    public void testScanForBleDevices() {
        ScanResult savedResult = mock(ScanResult.class);
        Map<String, ScanResult> resultMap = new HashMap<>();
        resultMap.put("EB:22:19:0E:B2:01", savedResult);
        mScanner.mAddressToScanResult = resultMap;

        mScanner.scanForBleDevices();

        verify(mHandler, times(1)).removeCallbacks(mScanner.mStopScanAction);
        verify(mAndroidScanner, times(1)).stopScan(mScanner.mScanCallback);
        assertTrue(resultMap.size() == 0); // make sure saved result was cleared

        verify(mAndroidScanner, times(1)).startScan(mScanner.mScanCallback);
        verify(mHandler, times(1))
                .postDelayed(mScanner.mStopScanAction, BleScanner.BLE_SCAN_DURATION_MS);
    }

    @Test
    public void testStopBleScan() {
        mScanner.stopBleScan();

        verify(mAndroidScanner, times(1)).stopScan(mScanner.mScanCallback);
        verify(mHandler, times(1)).removeCallbacks(mScanner.mStopScanAction);
    }
}
