package com.projectpluto.projectplutoandroid.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;

import com.projectpluto.projectplutoandroid.core.Permissions;

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
public class BluetoothServicePowerMockTest extends TestCase {
    @Mock BluetoothService btService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
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
}
