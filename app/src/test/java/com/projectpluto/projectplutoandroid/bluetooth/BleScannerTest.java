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

import java.util.HashSet;

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

    protected void setUp() throws Exception {
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
    public void testScanResult() throws Exception {
        ArgumentCaptor<BleScanner.ScanResultEvent> event
                = ArgumentCaptor.forClass(BleScanner.ScanResultEvent.class);

        ScanResult result = mock(ScanResult.class);
        when(result.getDevice()).thenReturn(mock(BluetoothDevice.class));
        mScanner.mScanCallback.onScanResult(1, result);

        verify(mBus, times(1)).post(event.capture());
        assertTrue(event.getValue().results.contains(result));
    }

    @Test
    public void testScanError() throws Exception {
        ArgumentCaptor<BleScanner.ScanFailureEvent> event
                = ArgumentCaptor.forClass(BleScanner.ScanFailureEvent.class);

        int errorCode = 5;
        mScanner.mScanCallback.onScanFailed(errorCode);

        verify(mBus, times(1)).post(event.capture());
        assertEquals(errorCode, event.getValue().errorCode);
    }

    @Test
    public void testStopScanAction() throws Exception {
        mScanner.mStopScanAction.run();

        verify(mAndroidScanner, times(1)).stopScan(mScanner.mScanCallback);
    }

    @Test
    public void testScanForBleDevices() throws Exception {
        ScanResult savedResult = mock(ScanResult.class);
        HashSet<ScanResult> resultSet = new HashSet<>();
        resultSet.add(savedResult);
        mScanner.mScanResultSet = resultSet;

        mScanner.scanForBleDevices();

        verify(mHandler, times(1)).removeCallbacks(mScanner.mStopScanAction);
        verify(mAndroidScanner, times(1)).stopScan(mScanner.mScanCallback);
        assertTrue(resultSet.size() == 0); // make sure saved result was cleared

        verify(mAndroidScanner, times(1)).startScan(mScanner.mScanCallback);
        verify(mHandler, times(1))
                .postDelayed(mScanner.mStopScanAction, BleScanner.BLE_SCAN_DURATION_MS);
    }

    @Test
    public void testStopBleScan() throws Exception {
        mScanner.stopBleScan();

        verify(mAndroidScanner, times(1)).stopScan(mScanner.mScanCallback);
        verify(mHandler, times(1)).removeCallbacks(mScanner.mStopScanAction);
    }
}
