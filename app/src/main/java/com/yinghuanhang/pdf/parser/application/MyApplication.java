package com.yinghuanhang.pdf.parser.application;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import com.yinghuanhang.pdf.parser.db.DaoMaster;
import com.yinghuanhang.pdf.parser.db.DaoSession;

/**
 * Created by think on 2018/2/6.
 */

public class MyApplication extends Application {

    private static DaoSession daoSession;
//    private static MyApplication mApp;

    @Override
    public void onCreate() {
        super.onCreate();
//        mApp = this;
        initDb();

    }

    private void initDb() {
        DaoMaster.DevOpenHelper devOpenHelper = new DaoMaster.DevOpenHelper(this, "parser.db");
        SQLiteDatabase database = devOpenHelper.getReadableDatabase();
        DaoMaster daoMaster = new DaoMaster(database);
        daoSession = daoMaster.newSession();
    }

//    public static MyApplication getInstance() {
//        return mApp;
//    }

    public static DaoSession getDaoSession() {
        return daoSession;
    }
}
