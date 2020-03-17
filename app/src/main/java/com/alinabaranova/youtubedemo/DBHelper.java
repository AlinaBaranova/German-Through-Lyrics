package com.alinabaranova.youtubedemo;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class DBHelper extends SQLiteAssetHelper {

    public static final String DBNAME = "app.db";
    public static final int DBVERSION = 1;

    public DBHelper(Context context) {
        super(context, DBNAME, null, DBVERSION);
    }

}
