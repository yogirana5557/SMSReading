package com.sms.reading.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;

import com.sms.reading.SMSApplication;
import com.sms.reading.model.Account;
import com.sms.reading.model.ParseSms;
import com.sms.reading.model.ShortSms;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;

public class SmsTable {
    private static final String TAG;
    private static SmsTable sInstance;

    static {
        TAG = SmsTable.class.getSimpleName();
    }

    public String[] allColumns;
    String[] modifiedColumnName;
    String smsAccountTable;
    private SQLiteDatabase database;
    private DBHelper dbHelper;

    private SmsTable(DBHelper dbh) {
        this.database = null;
        this.allColumns = new String[]{"_id", "smsId", "sender", "date", "body", "lat", "long", "locAccuracy", "tags", "accountId", "parsed", "UUID", "modifyCount", "smsFlags", "patternUID", "previousUUID"};
        this.smsAccountTable = "SmsAccount";
        this.modifiedColumnName = new String[]{"walnutSms_UUID", this.smsAccountTable + "_" + "name", this.smsAccountTable + "_" + "displayName", this.smsAccountTable + "_" + "pan", this.smsAccountTable + "_" + "displayPan", this.smsAccountTable + "_" + "type", this.smsAccountTable + "_" + "flags", this.smsAccountTable + "_" + "startDate", this.smsAccountTable + "_" + "endDate", this.smsAccountTable + "_" + "enabled", this.smsAccountTable + "_" + "UUID", this.smsAccountTable + "_" + "MUUID", this.smsAccountTable + "_" + "balance", this.smsAccountTable + "_" + "outstandingBalance", this.smsAccountTable + "_" + "balLastSyncTime", this.smsAccountTable + "_" + "outBalLastSyncTime", this.smsAccountTable + "_" + "updatedTime", this.smsAccountTable + "_" + "accountColor", this.smsAccountTable + "_" + "cardIssuer", "walnutSms_modifyCount"};
        this.dbHelper = dbh;
    }

    public static SmsTable getInstance(DBHelper dbh) {
        if (sInstance == null) {
            sInstance = new SmsTable(dbh);
            sInstance.database = dbh.getWritableDatabase();
        }
        return sInstance;
    }

    public static void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating Table : create table if not exists walnutSms(_id integer primary key autoincrement,smsId integer not null,sender text not null,date integer not null,body text not null,lat double default 360,long double default 360,locAccuracy double default -1, tags text not null default other,accountId integer not null,parsed boolean default 0,UUID text,modifyCount integer default 1,smsFlags integer default 0,patternUID  integer default 0,previousUUID text);");
        db.execSQL("create table if not exists walnutSms(_id integer primary key autoincrement,smsId integer not null,sender text not null,date integer not null,body text not null,lat double default 360,long double default 360,locAccuracy double default -1, tags text not null default other,accountId integer not null,parsed boolean default 0,UUID text,modifyCount integer default 1,smsFlags integer default 0,patternUID  integer default 0,previousUUID text);");
        db.execSQL("create trigger if not exists SmsTriggerModifiedFlag After update on walnutSms for each row  Begin  Update walnutSms Set modifyCount = modifyCount + 1  Where _id =  New._id;  End; ");
    }

    public static void onUpgrade(Context context, SQLiteDatabase database, int oldVersion, int newVersion) {

    }

    public static ShortSms cursorToSms(Cursor c) {
        boolean z = true;
        int _id = c.getInt(c.getColumnIndexOrThrow("_id"));
        String from = c.getString(c.getColumnIndexOrThrow("sender"));
        Date date = new Date(c.getLong(c.getColumnIndexOrThrow("date")));
        String body = c.getString(c.getColumnIndexOrThrow("body"));
        int accId = c.getInt(c.getColumnIndexOrThrow("accountId"));
        long smsId = c.getLong(c.getColumnIndexOrThrow("smsId"));
        int smsFlag = c.getInt(c.getColumnIndexOrThrow("smsFlags"));
        String UUID = c.getString(c.getColumnIndexOrThrow("UUID"));
        String previousUUID = c.getString(c.getColumnIndexOrThrow("previousUUID"));
        ShortSms sms = new ShortSms(from, body, date);
        sms.setSmsId(smsId);
        sms.set_id((long) _id);
        sms.setAccountId(accId);
        if (c.getInt(c.getColumnIndexOrThrow("parsed")) != 1) {
            z = false;
        }
        sms.setParsed(z);
        sms.setSmsFlag(smsFlag);
        sms.setSmsUUID(UUID);
        sms.setSmsPreviousUUID(previousUUID);
        return sms;
    }

    public static void searchOtpMessageAndEnableSplitFeature(Context context, SQLiteDatabase database) {
        String str = "walnutSms";
        SQLiteDatabase sQLiteDatabase = database;
        Cursor c = sQLiteDatabase.query(str, new String[]{"sender", "date", "body"}, "sender LIKE '%Walnut'", null, null, null, "date DESC");
        if (c != null) {
            if (c.getCount() > 0) {
                c.moveToFirst();
                SMSApplication.getInstance().setupRules();
                while (!c.isAfterLast()) {
                    ArrayList<ShortSms> parsedSmsList = ParseSms.Parse(context, c.getString(c.getColumnIndexOrThrow("sender")), c.getString(c.getColumnIndexOrThrow("body")), new Date(c.getLong(c.getColumnIndexOrThrow("date"))));
                    boolean found = false;
                    if (parsedSmsList != null) {
                        Iterator it = parsedSmsList.iterator();
                        while (it.hasNext()) {
                            ShortSms parsedSms = (ShortSms) it.next();

                        }
                    }

                    c.moveToNext();
                }
            }
            c.close();
        }
    }

    private static boolean isBlackListed(String body) {
        if (!body.matches("(?i).*[^a-z](password|otp|verification|activation|passcode)[^a-z].*")) {
            return false;
        }
        Log.d(TAG, "*** SMS BlackListed ***");
        return true;
    }

    public long writeSmsToDb(Account account, ShortSms sms, boolean parsed) {
        ContentValues values = new ContentValues();
        values.put("sender", sms.getNumber());
        values.put("date", Long.valueOf(sms.getDate().getTime()));
        if (isBlackListed(sms.getBody())) {
            sms.setSmsFlag(sms.getSmsFlag() | 2);
            values.put("body", "");
        } else {
            values.put("body", sms.getBody());
        }
        values.put("smsId", Long.valueOf(sms.getSmsId()));
        values.put("accountId", Integer.valueOf(account.get_id()));
        values.put("smsFlags", Integer.valueOf(sms.getSmsFlag()));
        values.put("parsed", Boolean.valueOf(parsed));
        Location loc = sms.getLocation();
        if (loc != null) {
            values.put("lat", Double.valueOf(loc.getLatitude()));
            values.put("long", Double.valueOf(loc.getLongitude()));
            values.put("locAccuracy", Float.valueOf(loc.getAccuracy()));
        }
        values.put("UUID", UUID.randomUUID().toString());
        if (!(sms.getRule() == null || sms.getRule().getPatternUID() == 0)) {
            values.put("patternUID", Long.valueOf(sms.getRule().getPatternUID()));
        }
        return this.database.insert("walnutSms", null, values);
    }

    public long getCount() {
        return DatabaseUtils.queryNumEntries(this.database, "walnutSms");
    }

    public long getParsedCount() {
        return DatabaseUtils.longForQuery(this.database, "SELECT COUNT(*) FROM walnutSms WHERE parsed = '1'", null);
    }

    public ArrayList<ShortSms> getAllMessagesOfAccount(Account account) {
        ArrayList<ShortSms> smslist = new ArrayList();
        String[] selectionArgs = new String[]{String.valueOf(account.get_id())};
        Cursor cursor = this.database.query("walnutSms", this.allColumns, "accountId =? AND smsFlags & 2 =0", selectionArgs, null, null, "_id DESC");
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            smslist.add(cursorToSms(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return smslist;
    }

    public ArrayList<ShortSms> getMessagesWithQuery(String query) {
        ArrayList<ShortSms> smslist = new ArrayList();
        Cursor cursor = this.database.rawQuery(query, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            smslist.add(cursorToSms(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return smslist;
    }


    private long getExistingSmsId(String number, String body, Date date, int puid) {
        String[] columns = new String[]{"_id"};
        String[] selectionArgs = new String[]{String.valueOf(date.getTime()), number.toUpperCase(), body, String.valueOf(puid)};
        long id = -1;
        Cursor cursor = this.database.query("walnutSms", columns, "date =? AND sender =? AND body =? AND patternUID =? ", selectionArgs, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                id = (long) cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return id;
    }


    public Cursor getMessagesCursor(boolean unParsedOnly) {
        String where = null;
        if (unParsedOnly) {
            where = "parsed='0' AND smsFlags & 2 =0";
        }
        return this.database.query("walnutSms", this.allColumns, where, null, null, null, "_id ASC");
    }

    public int updateMessage(long _id, ContentValues values) {
        if (_id >= 0) {
            return this.database.update("walnutSms", values, "_id = " + _id, null);
        }
        Log.d(TAG, "****ERROR**** Did not update SMS : " + values);
        return -1;
    }

    public void updateSmsMarkAsReported(ShortSms shortSms) {
        if (shortSms != null) {
            this.database.execSQL("update walnutSms set smsFlags = smsFlags | 1 where _id = " + shortSms.get_id());
        }
    }

    public ShortSms getSmsById(long _id) {
        Cursor cursor = this.database.query("walnutSms", this.allColumns, "_id = " + _id, null, null, null, null);
        if (cursor == null || cursor.getCount() <= 0) {
            if (cursor != null) {
                cursor.close();
            }
            return null;
        }
        cursor.moveToFirst();
        ShortSms newSms = null;
        if (!cursor.isAfterLast()) {
            newSms = cursorToSms(cursor);
        }
        cursor.close();
        return newSms;
    }

    public String getSmsUUID(long _id) {
        Cursor cursor = this.database.query("walnutSms", new String[]{"UUID"}, "_id = " + _id, null, null, null, null);
        if (cursor == null || cursor.getCount() <= 0) {
            if (cursor != null) {
                cursor.close();
            }
            return null;
        }
        cursor.moveToFirst();
        String uuid = cursor.getString(0);
        cursor.close();
        return uuid;
    }

    public int deleteMessage(long _id) {
        return this.database.delete("walnutSms", "_id = " + _id, null);
    }

    public void refreshTable(SQLiteDatabase database) {
        database.beginTransaction();
        database.execSQL("drop table if exists walnutSms");
        database.execSQL("drop trigger if exists SmsTriggerModifiedFlag");
        onCreate(database);
        database.setTransactionSuccessful();
        database.endTransaction();
    }

    public void updateAllShortSmsModifyCountFlag(long startTime, long endTime) {
        StringBuilder builder = new StringBuilder(200);
        builder.append("UPDATE walnutSms").append(" SET modifyCount = modifyCount + 1 ").append(" WHERE parsed = 0 ");
        if (!(endTime == -1 && startTime == 0)) {
            if (endTime == -1) {
                builder.append(" AND date >= " + startTime);
            } else {
                builder.append(" AND date >= " + startTime);
                builder.append(" AND date < " + endTime);
            }
        }
        this.database.execSQL(builder.toString());
    }


    public long restoreSms(String UUID, ContentValues values) {
        Cursor cursor = this.database.query("walnutSms", new String[]{"_id"}, "UUID =?", new String[]{UUID}, null, null, null);
        if (cursor == null || cursor.getCount() <= 0) {
            if (cursor != null) {
                cursor.close();
            }
            long smsId = getExistingSmsId(values.getAsString("sender"), values.getAsString("body"), new Date(values.getAsLong("date").longValue()), values.getAsInteger("patternUID").intValue());
            if (smsId == -1) {
                smsId = this.database.insert("walnutSms", null, values);
            }
            return smsId;
        }
        cursor.moveToFirst();
        long id = (long) cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
        cursor.close();
        return id;
    }


    public long getBackupCount(boolean backedUp) {
        String whereClause;
        if (backedUp) {
            whereClause = "modifyCount == 0  AND parsed = 0 ";
        } else {
            whereClause = "modifyCount > 0  AND parsed = 0 ";
        }
        Cursor cursor = this.database.query("walnutSms", new String[]{"COUNT( _id)"}, whereClause, null, null, null, null);
        cursor.moveToFirst();
        int count = 0;
        if (!cursor.isAfterLast()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return (long) count;
    }

    public ShortSms getSmsByUUID(String UUID) {
        if (UUID == null) {
            return null;
        }
        ShortSms newSms = null;
        Cursor cursor = this.database.query("walnutSms", this.allColumns, "UUID =?", new String[]{UUID}, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                newSms = cursorToSms(cursor);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return newSms;
    }
}
