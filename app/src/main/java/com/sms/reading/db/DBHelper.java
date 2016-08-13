package com.sms.reading.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Yogi on 30/07/2016.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = DBHelper.class.getSimpleName();
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "smsRead.db";
    private static DBHelper sInstance;
    private Context mContext;
    private SmsTable mSmsTable;

    private DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;
    }

    public static DBHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DBHelper(context);
            sInstance.mSmsTable = SmsTable.getInstance(sInstance);
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        SmsTable.onCreate(database);
    }

    public SmsTable getSmsTable() {
        return this.mSmsTable;
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void cleanTables() {
        SQLiteDatabase db = getWritableDatabase();
        this.mSmsTable.refreshTable(db);
    }

}
