package com.projectpluto.projectplutoandroid.ui.change_color;

import android.graphics.Color;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.projectpluto.projectplutoandroid.BuildConfig;
import com.projectpluto.projectplutoandroid.R;
import com.projectpluto.projectplutoandroid.bluetooth.BluetoothService;
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
public class ChangeColorActivityTest extends TestCase {
    @Mock Bus bus;
    @Mock ChangeColorPresenter presenter;
    @Mock BluetoothService service;
    @Mock SeekBar seekBar;
    ChangeColorActivity activity;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        activity = Robolectric.buildActivity(ChangeColorActivity.class).create().get();
        activity.mBus = bus;
    }

    @Test
    public void testOnCreate() {
        // Validate Butterknife bound views properly
        assertEquals(activity.mRedSeekBar.getId(), R.id.seekbar_red);
        assertEquals(activity.mGreenSeekBar.getId(), R.id.seekbar_green);
        assertEquals(activity.mBlueSeekBar.getId(), R.id.seekbar_blue);
    }

    @Test
    public void testOnServiceConected() {
        activity.mChangeClrPresenter = presenter;
        activity.mRedSeekBar = seekBar;
        activity.mBlueSeekBar = seekBar;
        activity.mGreenSeekBar = seekBar;
        activity.onServiceConnected(service);

        verify(presenter, times(1)).onServiceConnected(service);
        verify(seekBar, times(3)).setOnSeekBarChangeListener(activity.mListener);
    }

    @Test
    public void testOnStart() {
        activity.onStart();
        verify(bus, times(1)).register(activity.mChangeClrPresenter);
    }

    @Test
    public void testOnStop() {
        activity.onStop();
        verify(bus, times(1)).unregister(activity.mChangeClrPresenter);
    }

    @Test
    public void testOnSeekBarChangeListener() {
        activity.mChangeClrPresenter = presenter;
        activity.mRedSeekBar.setProgress(50);
        activity.mGreenSeekBar.setProgress(60);
        activity.mBlueSeekBar.setProgress(70);
        activity.mListener.onProgressChanged(null, 1, true);

        verify(presenter, times(1)).onSeekBarMove(50, 60, 70);
    }

    @Test
    public void testSetBackgroundColor() {
        PlutoColor color = new PlutoColor(1, 2, 3);
        activity.mBackground = mock(RelativeLayout.class);
        activity.setBackgroundColor(color);

        verify(activity.mBackground, times(1)).setBackgroundColor(color.getAndroidColor());
    }

    @Test
    public void testSetSeekbar() {
        PlutoColor color = new PlutoColor(1, 2, 3);
        activity.setSeekBar(color);

        assertEquals(Color.red(color.getAndroidColor()), activity.mRedSeekBar.getProgress());
        assertEquals(Color.green(color.getAndroidColor()), activity.mGreenSeekBar.getProgress());
        assertEquals(Color.blue(color.getAndroidColor()), activity.mBlueSeekBar.getProgress());
    }
}
