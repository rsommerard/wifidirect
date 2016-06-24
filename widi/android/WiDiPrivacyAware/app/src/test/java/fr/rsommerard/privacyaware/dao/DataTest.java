package fr.rsommerard.privacyaware.dao;

import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import fr.rsommerard.privacyaware.BuildConfig;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class DataTest {

    private DataDao mDataDao;
    private Data mData;

    @Before
    public void setup() {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(RuntimeEnvironment.application, "privacy-aware-db-test", null);
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();
        mDataDao = daoSession.getDataDao();

        mData = new Data();
        mData.setContent("Réfléchir, c'est fléchir deux fois.");
        mDataDao.insert(mData);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testContentRequirement() {
        Data data = new Data();
        mDataDao.insert(data);
    }

    @Test
    public void testGetContent() {
        assertEquals("should be the same content", "Réfléchir, c'est fléchir deux fois.", mData.getContent());
    }

    @Test
    public void testToString() {
        assertEquals("should be the data content", "Réfléchir, c'est fléchir deux fois.", mData.toString());
    }
}
