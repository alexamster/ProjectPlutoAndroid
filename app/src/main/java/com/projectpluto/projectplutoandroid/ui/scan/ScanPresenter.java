package com.projectpluto.projectplutoandroid.ui.scan;

import android.bluetooth.le.ScanResult;
import android.widget.Toast;

import com.projectpluto.projectplutoandroid.R;
import com.projectpluto.projectplutoandroid.bluetooth.BleScanner;
import com.projectpluto.projectplutoandroid.bluetooth.BluetoothService;
import com.projectpluto.projectplutoandroid.ui.change_color.ChangeColorActivity;
import com.projectpluto.projectplutoandroid.ui.common.BaseView;
import com.squareup.otto.Subscribe;

/**
 * Handles all business logic for the ScanActivity
 */
public class ScanPresenter {
    protected BluetoothService mBtService;
    protected IScanView mScanView;
    protected BaseView mBaseView;

    public ScanPresenter(IScanView scanView, BaseView baseView) {
        mScanView = scanView;
        mBaseView = baseView;
    }

    @Subscribe
    public void onScanResult(final BleScanner.ScanResultEvent event) {
        mScanView.updateScanResults(event.addressToResult.values());
    }

    /**
     * Called when service is connected and will enable the scan button
     */
    protected void onServiceConnected(BluetoothService service) {
        mBtService = service;
        mScanView.enableScanButton();
    }

    /**
     * Starts a BLE scan and notifies the user via toast
     */
    public void onClickStartScan() {
        mBtService.scanForBleDevices();
        mBaseView.toast(mBtService.getString(R.string.scanning), Toast.LENGTH_LONG);
    }

    /**
     * Initiates a connection with autoConnect = false
     */
    public void onClickConnect(ScanResult result) {
        mBtService.connect(result.getDevice(), false);
        mBaseView.startActivity(ChangeColorActivity.class);
    }
}
