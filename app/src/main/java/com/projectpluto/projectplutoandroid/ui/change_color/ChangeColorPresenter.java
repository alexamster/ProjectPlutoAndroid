package com.projectpluto.projectplutoandroid.ui.change_color;

import com.projectpluto.projectplutoandroid.R;
import com.projectpluto.projectplutoandroid.bluetooth.BleResultHandler;
import com.projectpluto.projectplutoandroid.bluetooth.BluetoothService;
import com.projectpluto.projectplutoandroid.bluetooth.PlutoCommunicator;
import com.projectpluto.projectplutoandroid.models.PlutoColor;
import com.projectpluto.projectplutoandroid.ui.common.BaseView;
import com.squareup.otto.Subscribe;

import timber.log.Timber;

/**
 * Handles all business logic for the ChangeColorActivity
 */
public class ChangeColorPresenter {
    protected BaseView mBaseView;
    protected IChangeColorView mChangeColorView;
    protected BluetoothService mBtService;

    public ChangeColorPresenter(IChangeColorView changeColorView, BaseView baseView) {
        mBaseView = baseView;
        mChangeColorView = changeColorView;
    }

    // Once the service connects we will read the color from the Pluto device.
    // onColorUpdate() will Update the UI to match the starting color.
    @Subscribe
    public void onColorUpdate(PlutoCommunicator.ColorUpdateEvent event) {
        if (event.color == null) {
            // Color will be null if it hasn't been read yet (Pluto Communicator always produces
            // event when we bind)
            return;
        }
        mChangeColorView.setBackgroundColor(event.color);
        mChangeColorView.setSeekBar(event.color);
    }

    // Result handler for color read requests. On failure Will popup an error, otherwise just log
    // success and wait for result.
    protected BleResultHandler mReadColorHandler = new BleResultHandler() {
        @Override
        public void onError() {
            mBaseView.popUp(mBtService.getString(R.string.app_name),
                    mBtService.getString(R.string.read_color_error));
        }

        @Override
        public void onSuccess() {
            Timber.d("Successfully sent read color command");
        }
    };

    public void onServiceConnected(BluetoothService btService) {
        mBtService = btService;
        mBtService.readColor(mReadColorHandler);
    }

    // Sends new color to Pluto device, and if the write is a success it will update the background
    // If the write is a failure the user will be notified via popup
    public void onSeekBarMove(int red, int green, int blue) {
        final PlutoColor color = new PlutoColor(red, green, blue);
        mBtService.changeColor(color, new BleResultHandler() {
            @Override
            public void onError() {
                mBaseView.popUp(mBtService.getString(R.string.app_name),
                        mBtService.getString(R.string.change_color_error));
            }

            @Override
            public void onSuccess() {
                mChangeColorView.setBackgroundColor(color);
            }
        });
    }
}
