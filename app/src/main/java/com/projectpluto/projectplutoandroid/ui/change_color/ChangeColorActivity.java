package com.projectpluto.projectplutoandroid.ui.change_color;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.projectpluto.projectplutoandroid.R;
import com.projectpluto.projectplutoandroid.bluetooth.BluetoothService;
import com.projectpluto.projectplutoandroid.core.BusProvider;
import com.projectpluto.projectplutoandroid.models.PlutoColor;
import com.projectpluto.projectplutoandroid.ui.common.BaseView;
import com.projectpluto.projectplutoandroid.ui.common.BluetoothServiceActivity;
import com.squareup.otto.Bus;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Activity used for changing the color of connected pluto devices
 *
 * It includes three seekbars which each control red, green and blue values respectively.
 * The background color will be updated to match the color on the device
 */
public class ChangeColorActivity extends BluetoothServiceActivity implements IChangeColorView {
    @Bind(R.id.seekbar_red) protected SeekBar mRedSeekBar;
    @Bind(R.id.seekbar_green) protected SeekBar mGreenSeekBar;
    @Bind(R.id.seekbar_blue) protected SeekBar mBlueSeekBar;
    @Bind(R.id.background) protected RelativeLayout mBackground;

    protected Bus mBus = BusProvider.getInstance();
    protected ChangeColorPresenter mChangeClrPresenter =
            new ChangeColorPresenter(this, new BaseView(this));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_color);
        ButterKnife.bind(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mBus.register(mChangeClrPresenter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBus.unregister(mChangeClrPresenter);
    }

    // This listener will be attached to all three seekbars, so moving any of them will trigger
    // a color update.
    SeekBar.OnSeekBarChangeListener mListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            mChangeClrPresenter.onSeekBarMove(mRedSeekBar.getProgress(),
                    mGreenSeekBar.getProgress(),
                    mBlueSeekBar.getProgress());
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
    };

    @Override
    protected void onServiceConnected(BluetoothService service) {
        mChangeClrPresenter.onServiceConnected(service);
        mRedSeekBar.setOnSeekBarChangeListener(mListener);
        mBlueSeekBar.setOnSeekBarChangeListener(mListener);
        mGreenSeekBar.setOnSeekBarChangeListener(mListener);
    }


    @Override
    public void setBackgroundColor(final PlutoColor color) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBackground.setBackgroundColor(color.getAndroidColor());
            }
        });
    }

    // Sets the seekbar values based on the RGB components of specified PlutoColor
    @Override
    public void setSeekBar(PlutoColor color) {
        final int androidColor = color.getAndroidColor();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRedSeekBar.setProgress(Color.red(androidColor));
                mGreenSeekBar.setProgress(Color.green(androidColor));
                mBlueSeekBar.setProgress(Color.blue(androidColor));
            }
        });
    }
}
