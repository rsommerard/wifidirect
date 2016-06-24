package fr.rsommerard.privacyaware.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import fr.rsommerard.privacyaware.WiFiDirect;
import fr.rsommerard.privacyaware.dao.DaoMaster;
import fr.rsommerard.privacyaware.dao.DaoMaster.DevOpenHelper;
import fr.rsommerard.privacyaware.dao.DaoSession;
import fr.rsommerard.privacyaware.dao.Data;
import fr.rsommerard.privacyaware.dao.DataDao;

public class DataManager {

    private final SecureRandom mRandom;
    private final DataDao mDataDao;

    public DataManager(final Context context) {
        DevOpenHelper helper = new DevOpenHelper(context, "privacy-aware-db", null);
        SQLiteDatabase mDb = helper.getWritableDatabase();
        DaoMaster mDaoMaster = new DaoMaster(mDb);
        DaoSession mDaoSession = mDaoMaster.newSession();
        mDataDao = mDaoSession.getDataDao();

        mDataDao.deleteAll(); // TODO: Delete this line

        mRandom = new SecureRandom();
    }

    public Data getData() {
        List<Data> data = mDataDao.loadAll();
        return data.get(mRandom.nextInt(data.size()));
    }

    public void removeData(final Data data) {
        mDataDao.delete(data);
    }

    public void removeData(final List<Data> data) {
        int size = data.size();
        for (int i = 0; i < size; i++) {
            Data d = data.get(i);
            mDataDao.delete(d);
        }
    }

    public List<Data> getAllData() {
        return mDataDao.loadAll();
    }

    public void addData(final Data data) {
        mDataDao.insert(data);
        Log.i(WiFiDirect.TAG, "Insert " + data.toString());
    }

    public boolean hasData() {
        return mDataDao.count() != 0;
    }

    public static String gsonify(List<Data> data) {
        GsonBuilder builder = new GsonBuilder();
        builder.serializeNulls();
        Gson gson = builder.create();

        Type arrayListType = new TypeToken<ArrayList<Data>>() {}.getType();

        return gson.toJson(data, arrayListType);
    }

    public static List<Data> deGsonify(final String gsonStr) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        Type arrayListType = new TypeToken<ArrayList<Data>>() {}.getType();

        return gson.fromJson(gsonStr, arrayListType);
    }
}
