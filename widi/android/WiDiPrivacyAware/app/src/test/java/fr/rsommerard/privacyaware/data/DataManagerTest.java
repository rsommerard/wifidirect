package fr.rsommerard.privacyaware.data;

import android.os.Build;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import fr.rsommerard.privacyaware.BuildConfig;
import fr.rsommerard.privacyaware.WiFiDirect;
import fr.rsommerard.privacyaware.dao.Data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class DataManagerTest {

    private DataManager mDataManager;

    @Before
    public void setup() {
        mDataManager = new DataManager(RuntimeEnvironment.application);

        Data data = new Data();
        data.setContent("This is the initial data.");
        mDataManager.addData(data);
    }

    @Test
    public void testSingleton() {
        DataManager dataManager = new DataManager(RuntimeEnvironment.application);

        assertEquals("should be the same reference", mDataManager, dataManager);
    }

    @Test
    public void testHasData() {
        assertTrue("should contain the initial data (at least)", mDataManager.hasData());
    }

    @Test
    public void testAddData() {
        List<Data> dataList = mDataManager.getAllData();
        int nbData = dataList.size();

        Data data = new Data();
        data.setContent("La volution.");

        mDataManager.addData(data);

        dataList = mDataManager.getAllData();

        assertEquals("should have one more data than beginning", nbData + 1, dataList.size());
    }

    @Test
    public void testGetData() {
        assertNotNull("should return a data", mDataManager.getData());
    }

    @Test
    public void testRemoveData() {
        Data data = new Data();
        data.setContent("Captp");

        mDataManager.addData(data);

        List<Data> dataList = mDataManager.getAllData();

        int nbData = dataList.size();

        mDataManager.removeData(data);
        dataList = mDataManager.getAllData();

        assertEquals("should remove the specific data", nbData - 1, dataList.size());
    }

    @Test
    public void testGsonSingleData() {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        Data data = new Data();
        data.setContent("Riviera détente.");

        String expected = "{\"content\":\"Riviera détente.\"}";
        assertEquals("should gson a single data", expected, gson.toJson(data));
    }

    /*@Test
    public void testGsonifySingleData() {
        Data data = new Data();
        data.setContent("Riviera détente.");

        String expected = "{\"content\":\"Riviera détente.\"}";
        assertEquals("should gson a single data", expected, DataManager.gsonify(data));
    }

    @Test
    public void testDeGsonifySingleData() {
        String gsonStr = "{\"content\":\"Riviera détente.\"}";

        Data data = DataManager.deGsonify(gsonStr);

        Data expected = new Data(null, "Riviera détente.");

        assertEquals("should gson a single data", expected.getContent(), data.getContent());
    }*/

    @Test
    public void testGsonifyListOfData() {
        List<Data> data = new ArrayList<>();
        data.add(new Data(null, "Époustouflant"));
        data.add(new Data(null, "Ornithorynque"));
        data.add(new Data(null, "Épitaphe"));

        String expected = "[{\"content\":\"Époustouflant\"},{\"content\":\"Ornithorynque\"},{\"content\":\"Épitaphe\"}]";
        assertEquals("should gson a list of data", expected, DataManager.gsonify(data));
    }

    @Test
    public void testGsonifyEmptyListOfData() {
        List<Data> data = new ArrayList<>();

        System.out.println(DataManager.gsonify(data));

        String expected = "[]";
        assertEquals("should gson a list of data", expected, DataManager.gsonify(data));
    }

    @Test
    public void testDeGsonifyListOfData() {
        String gsonStr = "[{\"content\":\"Époustouflant\"},{\"content\":\"Ornithorynque\"},{\"content\":\"Épitaphe\"}]";

        List<Data> data = DataManager.deGsonify(gsonStr);

        int expected = 3;

        assertEquals("should deGson a list of data", expected, data.size());
    }

    @Test
    public void testDeGsonifyEmptyListOfData() {
        String gsonStr = "[]";

        List<Data> data = DataManager.deGsonify(gsonStr);

        int expected = 0;

        assertEquals("should deGson a list of data", expected, data.size());
    }
}
