package com.projectpluto.projectplutoandroid.ui.common;

import android.content.ComponentName;

import com.projectpluto.projectplutoandroid.BuildConfig;
import com.projectpluto.projectplutoandroid.ConnectActivity;
import com.projectpluto.projectplutoandroid.R;
import com.projectpluto.projectplutoandroid.bluetooth.BluetoothService;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowToast;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class BluetoothServiceActivityTest extends TestCase {
    BluetoothServiceActivity activity;

    @Before
    public void setUp() {
        activity = Robolectric.buildActivity(ConnectActivity.class).create().get();
    }

    @Test
    public void testOnServiceConnected() {
        activity.mBtServiceConnection.onServiceConnected(
                mock(ComponentName.class),
                mock(BluetoothService.BluetoothServiceBinder.class));

        assertTrue(activity.mIsServiceBound);
    }

    @Test
    public void testOnServiceDisconnected() {
        ComponentName cmpName = mock(ComponentName.class);
        doReturn(BluetoothService.class.getSimpleName()).when(cmpName).getClassName();
        activity.mIsServiceBound = true;
        activity.mBtServiceConnection.onServiceDisconnected(cmpName);

        assertFalse(activity.mIsServiceBound);
        assertEquals(ShadowToast.getTextOfLatestToast(),
                RuntimeEnvironment.application.getString(R.string.service_exit_error));
    }

    @Test
    public void testOnDestroyIsBound() {
        activity.mIsServiceBound = true;
        activity.onDestroy();

        assertEquals(1, ShadowApplication.getInstance().getUnboundServiceConnections().size());
    }

    @Test
    public void testOnDestroyNotBound() {
        activity.mIsServiceBound = false;
        activity.onDestroy();

        assertEquals(0, ShadowApplication.getInstance().getUnboundServiceConnections().size());
    }
}
