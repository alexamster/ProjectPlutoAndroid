package com.projectpluto.projectplutoandroid.ui.scan;

import android.bluetooth.le.ScanResult;

import java.util.Collection;

public interface IScanView {
    void updateScanResults(final Collection<ScanResult> scanResults);
    void enableScanButton();
}
