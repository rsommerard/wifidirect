package fr.rsommerard.privacyaware.wifidirect.device;

import android.os.Build;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.util.List;

import fr.rsommerard.privacyaware.BuildConfig;
import fr.rsommerard.privacyaware.dao.Device;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class DeviceManagerTest {

    private DeviceManager mDeviceManager;

    @Before
    public void setup() {
        mDeviceManager = new DeviceManager(RuntimeEnvironment.application);

        Device device = new Device();
        device.setName("Android_ffa6");
        device.setAddress("7e:27:57:ae:57:ce");
        device.setTimestamp(Long.toString(System.currentTimeMillis()));

        mDeviceManager.addDevice(device);
    }

    @After
    public void tearDown() throws Exception {
        Field field = DeviceManager.class.getDeclaredField("sInstance");
        field.setAccessible(true);
        field.set(null, null);
    }

    @Test
    public void testSingleton() {
        DeviceManager deviceManager = new DeviceManager(RuntimeEnvironment.application);

        assertEquals("should be equals", mDeviceManager, deviceManager);
    }

    @Test
    public void testAddDevice() {
        Device device1 = createDevice1();
        mDeviceManager.addDevice(device1);

        List<Device> devices = mDeviceManager.getAllDevices();
        int nbDevices = devices.size();

        Device device2 = createDevice2();
        mDeviceManager.addDevice(device2);

        devices = mDeviceManager.getAllDevices();

        assertEquals("should be nbDevices + 1", nbDevices + 1, devices.size());
    }

    @Test
    public void testGetDevice() {
        assertNotNull("should return a device", mDeviceManager.getDevice());
    }

    @Test
    public void testContainDeviceWithNewTimestamp() {
        Device device = new Device();
        device.setName("Android_ffa6");
        device.setAddress("7e:27:57:ae:57:ce");
        device.setTimestamp(Long.toString(System.currentTimeMillis()));

        assertTrue("should contain this device", mDeviceManager.containsDevice(device));
    }

    @Test
    public void testUpdateDeviceWithNewTimestamp() {
        Device device = new Device();
        device.setName("Android_ffa6");
        device.setAddress("7e:27:57:ae:57:ce");
        device.setTimestamp(Long.toString(System.currentTimeMillis()));

        mDeviceManager.updateDevice(device);

        assertTrue(true);
    }

    @Test
    public void testHasDevices() {
        Device device = createDevice1();

        mDeviceManager.addDevice(device);

        assertTrue("should be true", mDeviceManager.hasDevices());
    }

    private Device createDevice1() {
        Device device = new Device();
        device.setName("Android_bea6");
        device.setAddress("7e:27:57:be:57:ce");
        device.setTimestamp(Long.toString(System.currentTimeMillis()));

        return device;
    }

    private Device createDevice2() {
        Device device = new Device();
        device.setName("Android_ffa6");
        device.setAddress("7e:45:57:ae:43:ce");
        device.setTimestamp(Long.toString(System.currentTimeMillis()));

        return device;
    }
}
