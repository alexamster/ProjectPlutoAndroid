package com.projectpluto.projectplutoandroid.ui.common;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

/**
 * BaseView is the container for common view tasks that will be used by most
 * activities, such as creating a text popup or toasting
 */
public class BaseView {
    protected AppCompatActivity mActivity;

    public BaseView(AppCompatActivity actvity) {
        mActivity = actvity;
    }

    public void toast(final String message, final int duration) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mActivity, message, duration).show();
            }
        });
    }

    public void popUp(final String title, final String body) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(mActivity)
                        .setMessage(body)
                        .setPositiveButton(mActivity.getString(android.R.string.ok), null)
                        .setTitle(title)
                        .show();
            }
        });
    }

    public void startActivity(Class target) {
        mActivity.startActivity(new Intent(mActivity, target));
    }
}
