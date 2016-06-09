package com.projectpluto.projectplutoandroid.models;

import android.graphics.Color;

/**
 * Translates between byte representation of a PlutoColor and its Android integer color value.
 */
public class PlutoColor {
    public static final byte COLOR_COMMAND = 0x61;
    private final byte[] mColorBytes;
    private final int mRed;
    private final int mBlue;
    private final int mGreen;

    // Androids Color.argb(int a, int r, int g, int b) uses integers so we will store them that way
    // and validate in constructor to fail fast.
    public PlutoColor(int red, int green, int blue) {
        if (red < 0 || green < 0 || blue < 0) {
            throw new IllegalArgumentException("Invalid color value: must be below 255");
        }

        if (red > 255 || green > 255 || blue > 255) {
            throw new IllegalArgumentException("Invalid color value: must be below 255");
        }

        this.mRed = red;
        this.mGreen = green;
        this.mBlue = blue;

        mColorBytes = new byte[4];
        mColorBytes[0] = COLOR_COMMAND;
        mColorBytes[1] = (byte)red;
        mColorBytes[2] = (byte)green;
        mColorBytes[3] = (byte)blue;
    }

    public PlutoColor(byte[] data) {
        if (data[0] != COLOR_COMMAND) {
            throw new IllegalArgumentException("Invalid color bytes: must start with color cmd");
        }

        mColorBytes = data;
        mRed = data[1];
        mGreen = data[2];
        mBlue = data[3];
    }

    public byte[] getBytes() {
        return mColorBytes;
    }

    public int getAndroidColor() {
        // default to 255 alpha so that there is no transparency
        return Color.argb(255, mRed, mGreen, mBlue);
    }

    @Override
    public String toString() {
        return String.format("RGB: (%s, %s, %s)", mRed, mGreen, mBlue);
    }
}
