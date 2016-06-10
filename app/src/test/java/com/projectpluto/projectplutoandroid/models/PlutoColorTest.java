package com.projectpluto.projectplutoandroid.models;

import android.graphics.Color;

import com.projectpluto.projectplutoandroid.BuildConfig;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricGradleTestRunner.class)
// robolectric is needed in order to use android.graphics.Color
@Config(constants = BuildConfig.class, sdk = 21)
public class PlutoColorTest extends TestCase {
    @Test
    public void testFromBytes() {
        byte[] data = new byte[4];
        data[0] = PlutoColor.COLOR_COMMAND;
        data[1] = 11;
        data[2] = 22;
        data[3] = 33;
        PlutoColor color = new PlutoColor(data);
        assertEquals(color.getBytes(), data);

        int androidColor = color.getAndroidColor();
        assertEquals(Color.red(androidColor), data[1]);
        assertEquals(Color.green(androidColor), data[2]);
        assertEquals(Color.blue(androidColor), data[3]);
    }

    @Test
    public void testFromRgb() {
        PlutoColor color = new PlutoColor(11, 0, 33);
        byte[] data = color.getBytes();

        assertEquals(data[0], PlutoColor.COLOR_COMMAND);
        assertEquals(data[1], 11);
        assertEquals(data[2], 0);
        assertEquals(data[3], 33);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromRgbInvalidRangeLarge() {
        PlutoColor color = new PlutoColor(11, 22, 400);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromRgbInvalidRangeNegative() {
        PlutoColor color = new PlutoColor(11, 22, -20);
    }
}
