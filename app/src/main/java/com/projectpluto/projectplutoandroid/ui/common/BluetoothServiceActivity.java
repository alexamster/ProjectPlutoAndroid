package com.projectpluto.projectplutoandroid.ui.common;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.projectpluto.projectplutoandroid.R;
import com.projectpluto.projectplutoandroid.bluetooth.BluetoothService;

/**
 * Base class for Activities that need to start/access the BluetoothService.
 *
 * The only requirement for extending is that Activities implement onServiceConnected(...) which
 * will be called once the service is connected. Service disconnects are handled here, notifying the
 * user and exiting the application.
 */
public abstract class BluetoothServiceActivity extends AppCompatActivity {
    protected boolean mIsServiceBound = false;

    protected ServiceConnection mBtServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            // In tests using Robolectric, calls to unbind service will result in null name
            if (null == name) {
                return;
            }

            if (name.getClassName().equals(BluetoothService.class.getSimpleName())) {
                mIsServiceBound = false;
                // Something went wrong, notify user and finish
                Toast.makeText(BluetoothServiceActivity.this,
                        getString(R.string.service_exit_error),
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // In tests, using Robolectric.buildActivity(...).create() will cause onServiceConnected
            // to get called with a null service
            if (null == service) {
                return;
            }

            mIsServiceBound = true;
            BluetoothService.BluetoothServiceBinder coreBinder =
                    (BluetoothService.BluetoothServiceBinder)service;

            BluetoothService btService = coreBinder.getService();
            BluetoothServiceActivity.this.onServiceConnected(btService);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = new Intent(this, BluetoothService.class);
        // Call startService first so that the service stays alive after the activity is destroyed.
        // This way we will continue to receive data transmitted from the pluto device when the app
        // is not in the foreground.
        startService(i);
        bindService(i, mBtServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Activity may have been destroyed before service bound, so check before unbinding
        if (mIsServiceBound) {
            unbindService(mBtServiceConnection);
        }
    }

    protected abstract void onServiceConnected(BluetoothService service);
}
