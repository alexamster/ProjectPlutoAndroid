package com.projectpluto.projectplutoandroid.ui.change_color;

import android.graphics.Color;

import com.projectpluto.projectplutoandroid.BuildConfig;
import com.projectpluto.projectplutoandroid.R;
import com.projectpluto.projectplutoandroid.bluetooth.BleResultHandler;
import com.projectpluto.projectplutoandroid.bluetooth.BluetoothService;
import com.projectpluto.projectplutoandroid.bluetooth.PlutoCommunicator;
import com.projectpluto.projectplutoandroid.models.PlutoColor;
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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;


@RunWith(RobolectricGradleTestRunner.class)
// robolectric is needed to use android.graphics.Color in testOnSeekBarMoved
@Config(constants = BuildConfig.class, sdk = 21)
public class ChangeColorPresenterTest extends TestCase {
    @Mock IChangeColorView view;
    @Mock BaseView baseView;
    @Mock BluetoothService service;
    @Captor ArgumentCaptor<PlutoColor> colorCaptor;
    @Captor ArgumentCaptor<BleResultHandler> handlerCaptor;
    ChangeColorPresenter presenter;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        presenter = new ChangeColorPresenter(view, baseView);
    }

    @Test
    public void testOnColorUpdate() {
        PlutoColor color = new PlutoColor(1, 2, 3);
        presenter.onColorUpdate(new PlutoCommunicator.ColorUpdateEvent(color));

        verify(view, times(1)).setBackgroundColor(color);
        verify(view, times(1)).setSeekBar(color);
    }

    @Test
    public void testOnColorUpdateNullColor() {
        presenter.onColorUpdate(new PlutoCommunicator.ColorUpdateEvent(null));

        verifyZeroInteractions(view);
    }

    @Test
    public void testBleResultHandlerError() {
        presenter.mBtService = service;
        doReturn("app").when(service).getString(R.string.app_name);
        doReturn("read_error").when(service).getString(R.string.read_color_error);
        presenter.mReadColorHandler.onError();

        verify(baseView, times(1)).popUp("app", "read_error");
    }

    @Test
    public void testOnServiceConnected() {
        presenter.onServiceConnected(mock(BluetoothService.class));

        verify(presenter.mBtService, times(1)).readColor(presenter.mReadColorHandler);
    }

    @Test
    public void testOnSeekBarMovedChangesCOlor() {
        presenter.mBtService = service;
        presenter.onSeekBarMove(50, 100, 255);

        verify(service, times(1)).changeColor(colorCaptor.capture(), handlerCaptor.capture());
        assertPlutoColorEqual(50, 100, 255, colorCaptor.getValue());
    }

    @Test
    public void testOnSeekBarMovedOnSuccess() {
        presenter.mBtService = service;
        presenter.onSeekBarMove(50, 100, 255);
        verify(service, times(1)).changeColor(any(PlutoColor.class), handlerCaptor.capture());
        handlerCaptor.getValue().onSuccess();

        verify(view, times(1)).setBackgroundColor(colorCaptor.capture());
        assertPlutoColorEqual(50, 100, 255, colorCaptor.getValue());
    }

    @Test
    public void testOnSeekBarMovedOnError() {
        doReturn("app").when(service).getString(R.string.app_name);
        doReturn("write_error").when(service).getString(R.string.change_color_error);
        presenter.mBtService = service;
        presenter.onSeekBarMove(50, 100, 255);
        verify(service, times(1)).changeColor(any(PlutoColor.class), handlerCaptor.capture());
        handlerCaptor.getValue().onError();

        verify(baseView, times(1)).popUp("app", "write_error");
    }

    protected void assertPlutoColorEqual(int red, int green, int blue, PlutoColor color) {
        assertEquals(red, Color.red(color.getAndroidColor()));
        assertEquals(green, Color.green(color.getAndroidColor()));
        assertEquals(blue, Color.blue(color.getAndroidColor()));
    }
}
