package com.projectpluto.projectplutoandroid.ui.scan;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.view.View;
import android.widget.TextView;

import com.projectpluto.projectplutoandroid.BuildConfig;
import com.projectpluto.projectplutoandroid.R;
import com.projectpluto.projectplutoandroid.bluetooth.BluetoothService;
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

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class ScanActivityTest extends TestCase {
    @Mock ScanPresenter presenter;
    @Mock Bus bus;
    ScanActivity activity;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        activity = Robolectric.buildActivity(ScanActivity.class).create().get();
        activity.mBus = bus;
    }

    @Test
    public void testOnCreate() {
        // Make sure adapter was created
        assertEquals(activity.mDeviceList.getAdapter(), activity.mAdapter);
        assertNotNull(activity.mScanPresenter);

        // Validate Butterknife bound views properly
        assertEquals(activity.mFab.getId(), R.id.fab);
        assertEquals(activity.mDeviceList.getId(), R.id.device_list);
    }

    @Test
    public void testOnServiceConnected() {
        BluetoothService service = mock(BluetoothService.class);
        activity.mScanPresenter = presenter;
        activity.onServiceConnected(service);

        verify(presenter, times(1)).onServiceConnected(service);
    }

    @Test
    public void testOnStart() {
        activity.onStart();
        verify(bus, times(1)).register(activity.mScanPresenter);
    }

    @Test
    public void testOnStop() {
        activity.onStop();
        verify(bus, times(1)).unregister(activity.mScanPresenter);
    }

    @Test
    public void testUpdateScanResult() {
        List<ScanResult> results = new ArrayList<>();
        ScanResult result = mock(ScanResult.class);
        results.add(result);
        activity.updateScanResults(results);

        assertEquals(1, activity.mAdapter.getCount());
        assertEquals(result, activity.mAdapter.getItem(0));
    }

    @Test
    public void enableScanButton() {
        activity.mScanPresenter = presenter;
        activity.enableScanButton();
        activity.mFab.callOnClick();

        verify(presenter, times(1)).onClickStartScan();
    }

    @Test
    public void testGetListItemView() {
        activity.mScanPresenter = presenter;
        ScanResult result = mock(ScanResult.class);
        BluetoothDevice device = mock(BluetoothDevice.class);
        doReturn(device).when(result).getDevice();
        doReturn("testName").when(device).getName();
        activity.mAdapter.add(result);
        View view = activity.mAdapter.getView(0, null, null);
        view.callOnClick();

        verify(presenter, times(1)).onClickConnect(result);
        TextView name = (TextView)view.findViewById(R.id.scan_result_name);
        assertEquals("testName", name.getText());

    }
}
