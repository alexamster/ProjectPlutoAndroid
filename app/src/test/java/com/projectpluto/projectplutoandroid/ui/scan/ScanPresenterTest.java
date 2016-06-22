package com.projectpluto.projectplutoandroid.ui.scan;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.widget.Toast;

import com.projectpluto.projectplutoandroid.BuildConfig;
import com.projectpluto.projectplutoandroid.R;
import com.projectpluto.projectplutoandroid.bluetooth.BleScanner;
import com.projectpluto.projectplutoandroid.bluetooth.BluetoothService;
import com.projectpluto.projectplutoandroid.ui.change_color.ChangeColorActivity;
import com.projectpluto.projectplutoandroid.ui.common.BaseView;

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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class ScanPresenterTest extends TestCase {
    @Mock BaseView baseView;
    @Mock BluetoothService bluetoothService;
    @Mock IScanView scanView;
    @Captor ArgumentCaptor<Collection<ScanResult>> scanResultCaptor;
    ScanPresenter presenter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        presenter = new ScanPresenter(scanView, baseView);
    }

    @Test
    public void testConstructor() {
        assertEquals(baseView, presenter.mBaseView);
        assertEquals(scanView, presenter.mScanView);
    }

    @Test
    public void testOnServiceConnected() {
        presenter.onServiceConnected(bluetoothService);

        verify(scanView, times(1)).enableScanButton();
        assertEquals(bluetoothService, presenter.mBtService);
    }

    @Test
    public void testOnScanResult() {
        ScanResult result1 = mock(ScanResult.class);
        ScanResult result2 = mock(ScanResult.class);
        Map<String, ScanResult> resultMap = new HashMap<>();
        resultMap.put("address1", result1);
        resultMap.put("address2", result2);
        BleScanner.ScanResultEvent event = new BleScanner.ScanResultEvent(resultMap);
        presenter.onScanResult(event);

        verify(scanView, times(1)).updateScanResults(scanResultCaptor.capture());
        assertEquals(scanResultCaptor.getValue(), resultMap.values());
    }

    @Test
    public void testStartScan() {
        doReturn("scanString").when(bluetoothService).getString(R.string.scanning);
        presenter.mBtService = bluetoothService;
        presenter.onClickStartScan();

        verify(bluetoothService, times(1)).scanForBleDevices();
        verify(baseView, times(1)).toast("scanString", Toast.LENGTH_LONG);
    }

    @Test
    public void testConnect() {
        ScanResult result = mock(ScanResult.class);
        BluetoothDevice device = mock(BluetoothDevice.class);
        doReturn(device).when(result).getDevice();
        presenter.mBtService = bluetoothService;
        presenter.onClickConnect(result);

        verify(bluetoothService, times(1)).connect(device, false);
        verify(baseView, times(1)).startActivity(ChangeColorActivity.class);
    }
}
