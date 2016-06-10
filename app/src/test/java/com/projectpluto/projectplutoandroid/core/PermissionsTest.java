package com.projectpluto.projectplutoandroid.core;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ContextCompat.class)
public class PermissionsTest extends TestCase {

    @Test
    public void testHasPermission() {
        PowerMockito.mockStatic(ContextCompat.class);
        PowerMockito.when(ContextCompat.checkSelfPermission(any(Context.class), anyString()))
                .thenReturn(PackageManager.PERMISSION_GRANTED);
        assertTrue(Permissions.hasCoarseLocation(mock(Context.class)));
    }

    @Test
    public void testDoesntHavePermission() {
        PowerMockito.mockStatic(ContextCompat.class);
        PowerMockito.when(ContextCompat.checkSelfPermission(any(Context.class), anyString()))
                .thenReturn(PackageManager.PERMISSION_DENIED);
        assertFalse(Permissions.hasCoarseLocation(mock(Context.class)));
    }
}
