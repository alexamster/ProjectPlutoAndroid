package com.projectpluto.projectplutoandroid.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import com.projectpluto.projectplutoandroid.BuildConfig;
import com.squareup.otto.Bus;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
// robolectric is needed or else BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
// is the same value as BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
@Config(constants = BuildConfig.class, sdk = 21)
public class BleCommunicatorTest extends TestCase {
    @Mock BleCommunicator bleCommunicator;
    @Mock BluetoothGatt gatt;
    @Mock BluetoothGattCharacteristic characteristic;
    @Mock BluetoothGattDescriptor descriptor;
    @Mock BleResultHandler handler;
    @Mock IBleConnectionStateListener connectionStateListener;
    @Mock Bus bus;

    @Captor ArgumentCaptor<BleCommunicator.ServiceDiscoveredEvent> discoveredCaptor;
    @Captor ArgumentCaptor<BleCommunicator.DescriptorWriteEvent> descWriteCaptor;
    @Captor ArgumentCaptor<BleCommunicator.CharacteristicChangedEvent> charaChangedCaptor;
    @Captor ArgumentCaptor<BleCommunicator.CharacteristicReadEvent> readCaptor;
    @Captor ArgumentCaptor<BleCommunicator.CharacteristicWriteEvent> writeCaptor;
    byte[] data = new byte[2];

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        doReturn(mock(BluetoothDevice.class)).when(gatt).getDevice();
        bleCommunicator.mRequestQueue = new LinkedBlockingQueue<>();
        bleCommunicator.mConnectionStateListener = connectionStateListener;
        bleCommunicator.mBus = bus;
    }

    private BleRequest getBleRequest(BleRequest.RequestType type) {
        byte[] reqData = null;
        if (BleRequest.RequestType.WRITE == type) {
            // only populate data for write requests
            reqData = data;
        }

        return new BleRequest(type,
                gatt,
                characteristic,
                reqData,
                handler);
    }

    @Test
    public void testConstructor() {
        IBleConnectionStateListener listener = new IBleConnectionStateListener() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {}
        };
        BleCommunicator bleCommunicator = new BleCommunicator(listener);
        assertEquals(bleCommunicator.mConnectionStateListener, listener);
    }

    @Test
    public void testEnqueueBleRequst() {
        doCallRealMethod().when(bleCommunicator).enqueueBleRequest(any(BleRequest.class));
        BleRequest request = getBleRequest(BleRequest.RequestType.READ);
        bleCommunicator.enqueueBleRequest(request);

        assertEquals(bleCommunicator.mRequestQueue.size(), 1);
        try {
            assertEquals(bleCommunicator.mRequestQueue.take(), request);
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testProcessNextReadRequestSuccess() {
        bleCommunicator.mRequestQueue.add(getBleRequest(BleRequest.RequestType.READ));
        doCallRealMethod().when(bleCommunicator).processNext();
        doReturn(true).when(gatt).readCharacteristic(characteristic);
        bleCommunicator.processNext();

        verify(gatt, times(1)).readCharacteristic(characteristic);
        verify(handler, times(1)).onSuccess();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testProcessNextWriteRequestSuccess() {
        bleCommunicator.mRequestQueue.add(getBleRequest(BleRequest.RequestType.WRITE));
        doCallRealMethod().when(bleCommunicator).processNext();
        doReturn(true).when(gatt).writeCharacteristic(characteristic);
        bleCommunicator.processNext();

        verify(characteristic, times(1)).setValue(data);
        verify(gatt, times(1)).writeCharacteristic(characteristic);
        verify(handler, times(1)).onSuccess();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testProcessNextSubscribeRequestSuccess() {
        bleCommunicator.mRequestQueue.add(getBleRequest(BleRequest.RequestType.SUBSCRIBE));
        doCallRealMethod().when(bleCommunicator).processNext();
        doReturn(descriptor).when(characteristic).getDescriptor(any(UUID.class));
        doReturn(true).when(gatt).writeDescriptor(descriptor);
        doReturn(true).when(descriptor).setValue(any(byte[].class));
        bleCommunicator.processNext();

        verify(gatt, times(1)).writeDescriptor(descriptor);
        verify(descriptor, times(1)).setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        verify(handler, times(1)).onSuccess();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testProcessNextUnsubscribeRequestSuccess() {
        bleCommunicator.mRequestQueue.add(getBleRequest(BleRequest.RequestType.UNSUBSCRIBE));
        doCallRealMethod().when(bleCommunicator).processNext();
        doReturn(descriptor).when(characteristic).getDescriptor(any(UUID.class));
        doReturn(true).when(gatt).writeDescriptor(descriptor);
        doReturn(true).when(descriptor).setValue(any(byte[].class));
        bleCommunicator.processNext();

        verify(gatt, times(1)).writeDescriptor(descriptor);
        verify(descriptor, times(1)).setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        verify(handler, times(1)).onSuccess();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testProcessNextReadRequestErrorThenSuccess() {
        bleCommunicator.mRequestQueue.add(getBleRequest(BleRequest.RequestType.READ));
        bleCommunicator.mRequestQueue.add(getBleRequest(BleRequest.RequestType.READ));
        doCallRealMethod().when(bleCommunicator).processNext();
        when(gatt.readCharacteristic(characteristic))
                .thenReturn(false)
                .thenReturn(true);
        bleCommunicator.processNext();

        verify(handler, times(1)).onError();
        verify(handler, times(1)).onSuccess();
        verify(gatt, times(2)).readCharacteristic(characteristic);
        verify(bleCommunicator, times(2)).processNext();
    }

    @Test
    public void testOnConnectionStateChanged() {
        doCallRealMethod().when(bleCommunicator).onConnectionStateChange(any(BluetoothGatt.class),
                anyInt(),
                anyInt());
        bleCommunicator.onConnectionStateChange(gatt, 1, 1);

        verify(connectionStateListener, times(1)).onConnectionStateChange(gatt, 1, 1);
    }

    @Test
    public void testOnServicesDiscovered() {
        doCallRealMethod().when(bleCommunicator).onServicesDiscovered(any(BluetoothGatt.class),
                anyInt());
        bleCommunicator.onServicesDiscovered(gatt, 1);

        verify(bus, times(1)).post(discoveredCaptor.capture());

        assertEquals(discoveredCaptor.getValue().gatt, gatt);
        assertEquals(discoveredCaptor.getValue().status, 1);
    }

    @Test
    public void testOnDescriptorWrite() {
        doCallRealMethod().when(bleCommunicator).onDescriptorWrite(gatt, descriptor, 1);
        bleCommunicator.onDescriptorWrite(gatt, descriptor, 1);

        verify(bus, times(1)).post(descWriteCaptor.capture());

        assertEquals(descWriteCaptor.getValue().gatt, gatt);
        assertEquals(descWriteCaptor.getValue().descriptor, descriptor);
        assertEquals(descWriteCaptor.getValue().status, 1);
    }

    @Test
    public void testOnCharacteristicChanged() {
        doCallRealMethod().when(bleCommunicator).onCharacteristicChanged(gatt, characteristic);
        bleCommunicator.onCharacteristicChanged(gatt, characteristic);

        verify(bus, times(1)).post(charaChangedCaptor.capture());

        assertEquals(charaChangedCaptor.getValue().gatt, gatt);
        assertEquals(charaChangedCaptor.getValue().characteristic, characteristic);
    }

    @Test
    public void testOnCharacteristicRead() {
        doCallRealMethod().when(bleCommunicator).onCharacteristicRead(gatt, characteristic, 1);
        bleCommunicator.onCharacteristicRead(gatt, characteristic, 1);

        verify(bus, times(1)).post(readCaptor.capture());

        assertEquals(readCaptor.getValue().gatt, gatt);
        assertEquals(readCaptor.getValue().characteristic, characteristic);
        assertEquals(readCaptor.getValue().status, 1);
    }

    @Test
    public void testOnCharacteristicWrite() {
        doCallRealMethod().when(bleCommunicator).onCharacteristicWrite(gatt, characteristic, 1);
        bleCommunicator.onCharacteristicWrite(gatt, characteristic, 1);

        verify(bus, times(1)).post(writeCaptor.capture());

        assertEquals(writeCaptor.getValue().gatt, gatt);
        assertEquals(writeCaptor.getValue().characteristic, characteristic);
        assertEquals(writeCaptor.getValue().status, 1);
    }
}
