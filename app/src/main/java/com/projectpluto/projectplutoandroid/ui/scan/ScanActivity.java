package com.projectpluto.projectplutoandroid.ui.scan;

import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.projectpluto.projectplutoandroid.R;
import com.projectpluto.projectplutoandroid.bluetooth.BluetoothService;
import com.projectpluto.projectplutoandroid.core.BusProvider;
import com.projectpluto.projectplutoandroid.ui.common.BaseView;
import com.projectpluto.projectplutoandroid.ui.common.BluetoothServiceActivity;
import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.Collection;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Activity used for scanning and initiating connections to Pluto BLE devices
 *
 * It includes a button to start a scan and will list scans result. Selecting an item on the
 * result list will initiate a connection using the selected scan result.
 */
public class ScanActivity extends BluetoothServiceActivity implements IScanView {
    @Bind(R.id.device_list) protected ListView mDeviceList;
    @Bind(R.id.fab) protected FloatingActionButton mFab;

    protected ArrayAdapter<ScanResult> mAdapter;
    protected ScanPresenter mScanPresenter;
    protected Bus mBus = BusProvider.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_connect);
        ButterKnife.bind(this);

        mScanPresenter = new ScanPresenter(this, new BaseView(this));
        mAdapter = new ScanResultAdapter(this, new ArrayList<ScanResult>());
        mDeviceList.setAdapter(mAdapter);
    }

    @Override
    protected void onServiceConnected(BluetoothService service) {
        mScanPresenter.onServiceConnected(service);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Presenter is tied to activity lifecycle
        mBus.register(mScanPresenter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBus.unregister(mScanPresenter);
    }

    /**
     * Replaces the contents of the ui scanResult list with specified scanResults
     */
    @Override
    public void updateScanResults(final Collection<ScanResult> scanResults) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.clear();
                mAdapter.addAll(scanResults);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void enableScanButton() {
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mScanPresenter.onClickStartScan();
            }
        });
    }

    public class ScanResultAdapter extends ArrayAdapter<ScanResult> {
        public ScanResultAdapter(Context context, ArrayList<ScanResult> users) {
            super(context, 0, users);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ScanResult scanResult = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.list_item_scanresult, parent, false);
            }

            TextView name = (TextView)convertView.findViewById(R.id.scan_result_name);
            name.setText(scanResult.getDevice().getName());
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mScanPresenter.onClickConnect(getItem(position));
                }
            });

            return convertView;
        }
    }
}
