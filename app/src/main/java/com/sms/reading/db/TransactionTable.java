package com.sms.reading.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.location.Location;
import android.text.TextUtils;
import android.util.Log;

import com.sms.reading.model.Account;
import com.sms.reading.model.AccountBalance;
import com.sms.reading.model.ChainingRule;
import com.sms.reading.model.ShortSms;
import com.sms.reading.model.Transaction;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;

public class TransactionTable {
    private static final String TAG = TransactionTable.class.getSimpleName();
    private static String[] allColumns;
    private static String[] joinedColumns;
    private static TransactionTable sInstance;

    static {
        allColumns = new String[]{"_id", "WalnutSmsId", "pos", "amount", "txnDate", "type", "flags", "accountId", "merchantId", "categories", "placeId", "placeName", "placeLat", "placeLon", "txnTags", "txnNote", "txnPhoto", "UUID", "modifyCount", "txnBalance", "txnOutstandingBalance", "txnPhotoServerPath"};
        joinedColumns = new String[]{"walnutTransactions._id", "WalnutSmsId", "pos", "amount", "txnDate", "walnutTransactions.type", "walnutTransactions.flags", "walnutTransactions.accountId", "merchantId", "categories", "placeId", "placeName", "placeLat", "placeLon", "txnTags", "txnNote", "txnPhoto", "txnPhotoServerPath", "txnBalance", "txnOutstandingBalance", "sender", "date", "body", "ifnull(lat, 360) as lat", "ifnull(long, 360) as long", "ifnull(locAccuracy, -1) as locAccuracy", "tags", "name", "displayName", "displayPan", "enabled", "walnutAccounts.flags as accountFlags", "walnutTransactions.UUID"};
    }

    String[] chainMatchingColumns;
    String smsAccountTable;
    String txnAccountTable;
    private String[] backupColumns;
    private SQLiteDatabase database;
    private DBHelper dbHelper;
    private String[] modifiedColumnName;

    private TransactionTable(DBHelper dbh) {
        this.database = null;
        this.chainMatchingColumns = new String[]{"walnutTransactions._id", "walnutTransactions.WalnutSmsId", "walnutTransactions.pos", "walnutTransactions.amount", "walnutTransactions.txnDate", "walnutTransactions.type", "walnutTransactions.flags", "walnutTransactions.accountId", "walnutTransactions.merchantId", "walnutTransactions.categories", "walnutTransactions.placeId", "walnutTransactions.placeName", "walnutTransactions.placeLat", "walnutTransactions.placeLon", "walnutTransactions.txnTags", "walnutTransactions.txnNote", "walnutTransactions.txnPhoto", "walnutTransactions.txnPhotoServerPath", "walnutTransactions.UUID", "walnutTransactions.modifyCount", "walnutTransactions.txnBalance", "walnutTransactions.txnOutstandingBalance", "pan"};
        this.smsAccountTable = "SmsAccount";
        this.txnAccountTable = "TxnAccount";
        this.modifiedColumnName = new String[]{"walnutTransactions_type", "walnutTransactions_flags", "walnutTransactions_UUID", this.txnAccountTable + "_" + "name", this.txnAccountTable + "_" + "displayName", this.txnAccountTable + "_" + "pan", this.txnAccountTable + "_" + "displayPan", this.txnAccountTable + "_" + "type", this.txnAccountTable + "_" + "flags", this.txnAccountTable + "_" + "startDate", this.txnAccountTable + "_" + "endDate", this.txnAccountTable + "_" + "enabled", this.txnAccountTable + "_" + "UUID", this.txnAccountTable + "_" + "MUUID", this.txnAccountTable + "_" + "balance", this.txnAccountTable + "_" + "outstandingBalance", this.txnAccountTable + "_" + "balLastSyncTime", this.txnAccountTable + "_" + "outBalLastSyncTime", this.txnAccountTable + "_" + "updatedTime", this.txnAccountTable + "_" + "accountColor", this.txnAccountTable + "_" + "cardIssuer", "walnutSms_UUID", this.smsAccountTable + "_" + "name", this.smsAccountTable + "_" + "displayName", this.smsAccountTable + "_" + "pan", this.smsAccountTable + "_" + "displayPan", this.smsAccountTable + "_" + "type", this.smsAccountTable + "_" + "flags", this.smsAccountTable + "_" + "startDate", this.smsAccountTable + "_" + "endDate", this.smsAccountTable + "_" + "enabled", this.smsAccountTable + "_" + "UUID", this.smsAccountTable + "_" + "MUUID", this.smsAccountTable + "_" + "balance", this.smsAccountTable + "_" + "outstandingBalance", this.smsAccountTable + "_" + "balLastSyncTime", this.smsAccountTable + "_" + "outBalLastSyncTime", this.smsAccountTable + "_" + "updatedTime", this.smsAccountTable + "_" + "accountColor", this.smsAccountTable + "_" + "cardIssuer", "walnutTransactions_modifyCount"};
        this.backupColumns = new String[]{"walnutTransactions.type AS " + this.modifiedColumnName[0], "walnutTransactions.flags AS " + this.modifiedColumnName[1], "walnutTransactions.UUID AS " + this.modifiedColumnName[2], this.txnAccountTable + "." + "name" + " AS " + this.modifiedColumnName[3], this.txnAccountTable + "." + "displayName" + " AS " + this.modifiedColumnName[4], this.txnAccountTable + "." + "pan" + " AS " + this.modifiedColumnName[5], this.txnAccountTable + "." + "displayPan" + " AS " + this.modifiedColumnName[6], this.txnAccountTable + "." + "type" + " AS " + this.modifiedColumnName[7], this.txnAccountTable + "." + "flags" + " AS " + this.modifiedColumnName[8], this.txnAccountTable + "." + "startDate" + " AS " + this.modifiedColumnName[9], this.txnAccountTable + "." + "endDate" + " AS " + this.modifiedColumnName[10], this.txnAccountTable + "." + "enabled" + " AS " + this.modifiedColumnName[11], this.txnAccountTable + "." + "UUID" + " AS " + this.modifiedColumnName[12], this.txnAccountTable + "." + "MUUID" + " AS " + this.modifiedColumnName[13], this.txnAccountTable + "." + "balance" + " AS " + this.modifiedColumnName[14], this.txnAccountTable + "." + "outstandingBalance" + " AS " + this.modifiedColumnName[15], this.txnAccountTable + "." + "balLastSyncTime" + " AS " + this.modifiedColumnName[16], this.txnAccountTable + "." + "outBalLastSyncTime" + " AS " + this.modifiedColumnName[17], this.txnAccountTable + "." + "updatedTime" + " AS " + this.modifiedColumnName[18], this.txnAccountTable + "." + "accountColor" + " AS " + this.modifiedColumnName[19], this.txnAccountTable + "." + "cardIssuer" + " AS " + this.modifiedColumnName[20], "walnutSms.UUID AS " + this.modifiedColumnName[21], this.smsAccountTable + "." + "name" + " AS " + this.modifiedColumnName[22], this.smsAccountTable + "." + "displayName" + " AS " + this.modifiedColumnName[23], this.smsAccountTable + "." + "pan" + " AS " + this.modifiedColumnName[24], this.smsAccountTable + "." + "displayPan" + " AS " + this.modifiedColumnName[25], this.smsAccountTable + "." + "type" + " AS " + this.modifiedColumnName[26], this.smsAccountTable + "." + "flags" + " AS " + this.modifiedColumnName[27], this.smsAccountTable + "." + "startDate" + " AS " + this.modifiedColumnName[28], this.smsAccountTable + "." + "endDate" + " AS " + this.modifiedColumnName[29], this.smsAccountTable + "." + "enabled" + " AS " + this.modifiedColumnName[30], this.smsAccountTable + "." + "UUID" + " AS " + this.modifiedColumnName[31], this.smsAccountTable + "." + "MUUID" + " AS " + this.modifiedColumnName[32], this.smsAccountTable + "." + "balance" + " AS " + this.modifiedColumnName[33], this.smsAccountTable + "." + "outstandingBalance" + " AS " + this.modifiedColumnName[34], this.smsAccountTable + "." + "balLastSyncTime" + " AS " + this.modifiedColumnName[35], this.smsAccountTable + "." + "outBalLastSyncTime" + " AS " + this.modifiedColumnName[36], this.smsAccountTable + "." + "updatedTime" + " AS " + this.modifiedColumnName[37], this.smsAccountTable + "." + "accountColor" + " AS " + this.modifiedColumnName[38], this.smsAccountTable + "." + "cardIssuer" + " AS " + this.modifiedColumnName[39], "walnutTransactions.modifyCount AS " + this.modifiedColumnName[40], "*"};
        this.dbHelper = dbh;
    }

    public static TransactionTable getInstance(DBHelper dbh) {
        if (sInstance == null) {
            sInstance = new TransactionTable(dbh);
            sInstance.database = dbh.getWritableDatabase();
        }
        return sInstance;
    }

    public static void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating Table : create table if not exists walnutTransactions(_id integer primary key autoincrement,WalnutSmsId integer not null, pos text not null,amount real not null,txnDate integer not null,type integer not null,flags integer default 0,accountId integer not null,merchantId integer default -1,categories text not null default other,placeId text default null,placeName text default null,placeLat double default 360,placeLon double default 360,txnTags text,txnNote text,txnPhoto text,UUID text,modifyCount integer default 1,txnBalance real,txnOutstandingBalance real,txnPhotoServerPath text);");
        db.execSQL("create table if not exists walnutTransactions(_id integer primary key autoincrement,WalnutSmsId integer not null, pos text not null,amount real not null,txnDate integer not null,type integer not null,flags integer default 0,accountId integer not null,merchantId integer default -1,categories text not null default other,placeId text default null,placeName text default null,placeLat double default 360,placeLon double default 360,txnTags text,txnNote text,txnPhoto text,UUID text,modifyCount integer default 1,txnBalance real,txnOutstandingBalance real,txnPhotoServerPath text);");
        db.execSQL("create trigger if not exists TxnTriggerModifiedFlag After update on walnutTransactions for each row  Begin  Update walnutTransactions Set modifyCount = modifyCount + 1  Where _id =  New._id;  End; ");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void onUpgrade(android.database.sqlite.SQLiteDatabase r12, int r13, int r14) {

    }

    public static Transaction cursorToJoinedTransaction(Cursor c) {
        int _id = c.getInt(c.getColumnIndexOrThrow("_id"));
        String from = c.getString(c.getColumnIndexOrThrow("sender"));
        Calendar cal1 = Calendar.getInstance();
        cal1.setTimeInMillis(c.getLong(c.getColumnIndexOrThrow("date")));
        Date date = cal1.getTime();
        String body = c.getString(c.getColumnIndexOrThrow("body"));
        String pos = c.getString(c.getColumnIndexOrThrow("pos"));
        double amount = c.getDouble(c.getColumnIndexOrThrow("amount"));
        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(c.getLong(c.getColumnIndexOrThrow("txnDate")));
        Date txnDate = cal2.getTime();
        int txnType = c.getInt(c.getColumnIndexOrThrow("type"));
        int txnFlags = c.getInt(c.getColumnIndexOrThrow("flags"));
        int accId = c.getInt(c.getColumnIndexOrThrow("accountId"));
        String pan = c.getString(c.getColumnIndex("displayPan"));
        String category = c.getString(c.getColumnIndexOrThrow("name"));
        int walnutSmsId = c.getInt(c.getColumnIndexOrThrow("WalnutSmsId"));
        String tag = c.getString(c.getColumnIndexOrThrow("tags"));
        double balance = c.getDouble(c.getColumnIndexOrThrow("txnBalance"));
        double outBalance = c.getDouble(c.getColumnIndexOrThrow("txnOutstandingBalance"));
        String accName = c.getString(c.getColumnIndexOrThrow("name"));
        String accDisplayName = c.getString(c.getColumnIndexOrThrow("displayName"));
        int columnIndex = c.getColumnIndex("accountFlags");
        boolean isExpenseAccount = true;
        if (columnIndex != -1) {
            isExpenseAccount = (c.getInt(columnIndex) & 16) == 0;
        }
        String txnUUID = c.getString(c.getColumnIndexOrThrow("UUID"));
        Transaction txn = new Transaction(from, body, date);
        txn.set_id((long) _id);
        txn.setAccountId(accId);
        txn.setAccountName(accName);
        txn.setAccountDisplayName(accDisplayName);
        txn.setSmsId((long) walnutSmsId);
        txn.setSmsType(3);
        txn.setCategories(category, "Spends");
        txn.setTransaction(pan, Double.valueOf(amount), txnDate, pos, txnType);
        txn.setFlags(txnFlags);
        AccountBalance bal = new AccountBalance();
        bal.setBalance(balance);
        bal.setOutstandingBalance(outBalance);
        txn.setBalance(bal);
        txn.setIsExpenseAcc(isExpenseAccount);
        txn.setUUID(txnUUID);
        txn.setMerchantId(c.getLong(c.getColumnIndexOrThrow("merchantId")));
        txn.setTxnCategories(c.getString(c.getColumnIndexOrThrow("categories")));
        txn.setTxnTags(c.getString(c.getColumnIndexOrThrow("txnTags")));
        txn.setTxnNote(c.getString(c.getColumnIndexOrThrow("txnNote")));
        txn.setTxnPhoto(c.getString(c.getColumnIndexOrThrow("txnPhoto")));
        txn.setTxnPhotoServerPath(c.getString(c.getColumnIndexOrThrow("txnPhotoServerPath")));
        txn.setPlaceId(c.getString(c.getColumnIndexOrThrow("placeId")));
        txn.setPlaceName(c.getString(c.getColumnIndexOrThrow("placeName")));
        double lat = c.getDouble(c.getColumnIndex("placeLat"));
        double lng = c.getDouble(c.getColumnIndex("placeLon"));
        if (!(lat == 360.0d || lng == 360.0d)) {
            Location location = new Location("walnutloc");
            location.setLatitude(lat);
            location.setLongitude(lng);
            txn.setPlaceLocation(location);
        }

        return txn;
    }

    public long writeTransactionToDb(Transaction txn) {
        Cursor cursor = null;
        SmsTable smsTable = this.dbHelper.getSmsTable();
        String[] columns;
        String whereClause;
        String[] selectionArgs;
        SQLiteQueryBuilder sqb;
        if (txn.hasAccurateDate()) {
            columns = new String[]{"walnutSms._id"};
            whereClause = "walnutSms._id < " + txn.getSmsId() + " AND " + "body" + " =? AND " + "walnutTransactions" + "." + "flags" + " & " + 16 + " = 0 ";
            selectionArgs = new String[]{txn.getBody()};
            sqb = new SQLiteQueryBuilder();
            sqb.setTables("walnutSms JOIN walnutTransactions ON walnutTransactions.WalnutSmsId = walnutSms._id");
            cursor = sqb.query(this.database, columns, whereClause, selectionArgs, null, null, null);
            if (cursor != null) {
                Log.d(TAG, "Found sms: " + cursor.getCount());
            }
            if (cursor != null && cursor.getCount() > 0) {
                Log.d(TAG, "HasAccurate, Duplicate TXN : " + txn.getPos() + " / " + txn.getAmount());
                txn.setDuplicate(true);
                txn.setIsNotAnExpense();
            }
        } else if (txn.hasPos() && txn.getDate() != null) {
            columns = new String[]{"walnutSms._id"};
            whereClause = "date > " + (txn.getDate().getTime() - 3600000) + " AND " + "walnutSms" + "." + "_id" + " < " + txn.getSmsId() + " AND " + "body" + " =? AND " + "walnutTransactions" + "." + "flags" + " & " + 16 + " = 0 ";
            selectionArgs = new String[]{txn.getBody()};
            sqb = new SQLiteQueryBuilder();
            sqb.setTables("walnutSms JOIN walnutTransactions ON walnutTransactions.WalnutSmsId = walnutSms._id");
            cursor = sqb.query(this.database, columns, whereClause, selectionArgs, null, null, "walnutSms._id DESC", "1");
            if (cursor != null && cursor.getCount() > 0) {
                Log.d(TAG, "HasPos, Duplicate TXN : " + txn.getPos() + " / " + txn.getAmount());
                txn.setDuplicate(true);
                txn.setIsNotAnExpense();
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        ContentValues values = new ContentValues();
        values.put("WalnutSmsId", Long.valueOf(txn.getSmsId()));
        values.put("pos", txn.getPos().toLowerCase());
        values.put("placeName", txn.getPlaceName());
        values.put("amount", Double.valueOf(txn.getAmount()));
        values.put("txnDate", Long.valueOf(txn.getTxnDate().getTime()));
        values.put("type", Integer.valueOf(txn.getTxnType()));
        values.put("flags", Integer.valueOf(txn.getFlags()));
        values.put("accountId", Integer.valueOf(txn.getAccountId()));
        values.put("txnNote", txn.getTxnNote());
        if (txn.getPlaceId() != null) {
            values.put("placeId", txn.getPlaceId());
        }
        if (txn.getMerchantId() != -1) {
            values.put("merchantId", Long.valueOf(txn.getMerchantId()));
        }
        if (txn.getTxnCategories() != null) {
            values.put("categories", txn.getTxnCategories());
        }
        if (txn.getLocation() != null) {
            values.put("placeLat", Double.valueOf(txn.getLocation().getLatitude()));
            values.put("placeLon", Double.valueOf(txn.getLocation().getLongitude()));
        }
        if (txn.getTxnTags() != null) {
            values.put("txnTags", txn.getTxnTags());
        }
        if (txn.getTxnNote() != null) {
            values.put("txnNote", txn.getTxnNote());
        }
        if (txn.getTxnPhoto() != null) {
            values.put("txnPhoto", txn.getTxnPhoto());
        }
        if (txn.getTxnPhotoServerPath() != null) {
            values.put("txnPhotoServerPath", txn.getTxnPhotoServerPath());
        }
        values.put("UUID", UUID.randomUUID().toString());
        if (txn.getBalance() != null) {
            values.put("txnBalance", Double.valueOf(txn.getBalance().getBalance()));
            values.put("txnOutstandingBalance", Double.valueOf(txn.getBalance().getOutstandingBalance()));
        }
        return this.database.insert("walnutTransactions", null, values);
    }

    public ArrayList<ShortSms> getTransactions(int[] accIds, int[] txnTypes, String posName, String placeName, Date startDate, Date endDate, boolean sortAscending, int limit) {
        Cursor cursor;
        String sortOrder = sortAscending ? "txnDate ASC" : "txnDate DESC";
        ArrayList<ShortSms> smslist = new ArrayList();
        String whereClause = "";
        ArrayList<String> selectionArgs = new ArrayList();
        boolean prependAnd = false;
        if ((accIds != null && accIds.length > 1) || accIds == null) {
            whereClause = "walnutAccounts.enabled=" + makePlaceholders(1);
            selectionArgs.add(String.valueOf(1));
            prependAnd = true;
        }
        whereClause = whereClause.concat((prependAnd ? " AND " : "name") + "walnutTransactions" + "." + "flags" + " & " + 16 + " = 0 ");
        prependAnd = true;
        if (txnTypes != null) {
            whereClause = whereClause.concat((prependAnd ? " AND " : "name") + "walnutTransactions" + "." + "type" + " IN (" + makePlaceholders(txnTypes.length) + ")");
            for (int type : txnTypes) {
                selectionArgs.add(String.valueOf(type));
            }
            prependAnd = true;
        }
        if (accIds != null) {
            whereClause = whereClause.concat((prependAnd ? " AND " : "name") + "walnutTransactions" + "." + "accountId" + " IN (" + this.dbHelper.getAccountTable().getHierarchicalChildAccountIdQuery(accIds, false) + ")");
            prependAnd = true;
        }
        if (posName != null) {
            whereClause = whereClause.concat((prependAnd ? " AND " : "name") + "( " + "pos" + " =?)");
            selectionArgs.add(posName.toLowerCase());
            prependAnd = true;
        }
        if (placeName != null) {
            whereClause = whereClause.concat((prependAnd ? " AND " : "name") + "( lower(" + "placeName" + ") =?)");
            selectionArgs.add(placeName.toLowerCase());
            prependAnd = true;
        }
        if (startDate != null) {
            whereClause = whereClause.concat((prependAnd ? " AND " : "name") + "txnDate" + " >=?");
            selectionArgs.add(String.valueOf(startDate.getTime()));
            prependAnd = true;
        }
        if (endDate != null) {
            whereClause = whereClause.concat((prependAnd ? " AND " : "name") + "txnDate" + " <=?");
            selectionArgs.add(String.valueOf(endDate.getTime()));
        }
        SQLiteQueryBuilder sqb = new SQLiteQueryBuilder();
        sqb.setTables("walnutTransactions JOIN walnutAccounts ON walnutTransactions.accountId = walnutAccounts._id LEFT OUTER JOIN walnutSms ON walnutTransactions.WalnutSmsId = walnutSms._id");
        if (limit > 0) {
            cursor = sqb.query(this.database, joinedColumns, whereClause, (String[]) selectionArgs.toArray(new String[0]), null, null, sortOrder, "name" + limit);
        } else {
            cursor = sqb.query(this.database, joinedColumns, whereClause, (String[]) selectionArgs.toArray(new String[0]), null, null, sortOrder);
        }
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            smslist.add(cursorToJoinedTransaction(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return smslist;
    }

    private String makePlaceholders(int len) {
        StringBuilder sb = new StringBuilder((len * 2) - 1);
        sb.append("?");
        for (int i = 1; i < len; i++) {
            sb.append(",?");
        }
        return sb.toString();
    }

    public int updateTransaction(Transaction txn, ContentValues values) {
        if (txn.get_id() >= 0) {
            return this.database.update("walnutTransactions", values, "_id = " + txn.get_id(), null);
        }
        Log.d(TAG, "****ERROR**** TXN not found - ID: " + txn.get_id() + "with" + values);
        return -1;
    }

    public int updateTransactionMerchant(Transaction txn) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("merchantId", Long.valueOf(txn.getMerchantId()));
        contentValues.put("categories", txn.getTxnCategories());
        contentValues.put("txnTags", txn.getTxnTags());
        contentValues.put("placeId", txn.getPlaceId());
        contentValues.put("placeName", txn.getPlaceName());
        if (txn.getPlaceLocation() != null) {
            contentValues.put("placeLat", Double.valueOf(txn.getPlaceLocation().getLatitude()));
            contentValues.put("placeLon", Double.valueOf(txn.getPlaceLocation().getLongitude()));
        }
        return updateTransaction(txn, contentValues);
    }

    public Transaction cursorToTransaction(Cursor c) {
        int _id = c.getInt(c.getColumnIndexOrThrow("_id"));
        int walnutSmsId = c.getInt(c.getColumnIndexOrThrow("WalnutSmsId"));
        String pos = c.getString(c.getColumnIndexOrThrow("pos"));
        double amount = c.getDouble(c.getColumnIndexOrThrow("amount"));
        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(c.getLong(c.getColumnIndexOrThrow("txnDate")));
        Date txnDate = cal2.getTime();
        int txnType = c.getInt(c.getColumnIndexOrThrow("type"));
        int txnFlags = c.getInt(c.getColumnIndexOrThrow("flags"));
        int accId = c.getInt(c.getColumnIndexOrThrow("accountId"));
        double balance = c.getDouble(c.getColumnIndexOrThrow("txnBalance"));
        double outBalance = c.getDouble(c.getColumnIndexOrThrow("txnOutstandingBalance"));
        Transaction txn = new Transaction(null, null, null);
        txn.set_id((long) _id);
        txn.setSmsId((long) walnutSmsId);
        txn.setAccountId(accId);
        txn.setSmsType(3);
        txn.setCategories(null, "Spends");
        txn.setTxnTags(c.getString(c.getColumnIndexOrThrow("txnTags")));
        txn.setTxnNote(c.getString(c.getColumnIndexOrThrow("txnNote")));
        txn.setTxnPhoto(c.getString(c.getColumnIndexOrThrow("txnPhoto")));
        txn.setTxnPhotoServerPath(c.getString(c.getColumnIndexOrThrow("txnPhotoServerPath")));
        txn.setTransaction(null, Double.valueOf(amount), txnDate, pos, txnType);
        txn.setFlags(txnFlags);
        AccountBalance bal = new AccountBalance();
        bal.setBalance(balance);
        bal.setOutstandingBalance(outBalance);
        txn.setBalance(bal);
        txn.setMerchantId(c.getLong(c.getColumnIndexOrThrow("merchantId")));
        txn.setPlaceId(c.getString(c.getColumnIndexOrThrow("placeId")));
        txn.setPlaceName(c.getString(c.getColumnIndexOrThrow("placeName")));
        double lat = c.getDouble(c.getColumnIndex("placeLat"));
        double lng = c.getDouble(c.getColumnIndex("placeLon"));
        if (!(lat == 360.0d || lng == 360.0d)) {
            Location location = new Location("walnutloc");
            location.setLatitude(lat);
            location.setLongitude(lng);
            txn.setPlaceLocation(location);
        }
        return txn;
    }

    public Transaction getLastMatchingTransaction(ArrayList<Account> accList, ArrayList<ChainingRule.MatchingCriteria> matchingCriterias, Transaction txn) {
        Log.d(TAG, "getLastMatchingTransaction");
        StringBuilder selection = new StringBuilder();
        if (!accList.isEmpty()) {
            selection.append("walnutTransactions.accountId IN(");
            for (int i = 0; i < accList.size(); i++) {
                selection.append(((Account) accList.get(i)).get_id());
                if (i == accList.size() - 1) {
                    selection.append(")");
                } else {
                    selection.append(",");
                }
            }
        }
        ArrayList<String> selectionArgs = new ArrayList();
        int deleteFilter = 0;
        Iterator it = matchingCriterias.iterator();
        while (it.hasNext()) {
            ChainingRule.MatchingCriteria matchingCriteria = (ChainingRule.MatchingCriteria) it.next();
            String parentField = getTransactionDBField(matchingCriteria.getParentField());
            String childField = getTransactionFieldValue(matchingCriteria.getChildField(), txn);
            String matchType = matchingCriteria.getMatchType();
            Log.d(TAG, parentField + " " + childField + " " + matchType);
            if (TextUtils.equals(parentField, "txnDate")) {
                if (!TextUtils.isEmpty(selection)) {
                    selection.append(" AND ");
                }
                long matchValue = matchingCriteria.getMatchValue();
                long startTime = Long.valueOf(childField).longValue() - matchValue;
                long endTime = Long.valueOf(childField).longValue() + matchValue;
                selection.append(parentField).append(" >=? AND ").append(parentField).append(" <=?");
                selectionArgs.add(String.valueOf(startTime));
                selectionArgs.add(String.valueOf(endTime));
            } else {
                if (TextUtils.equals(parentField, "deleted")) {
                    deleteFilter = matchingCriteria.getDeletedFilter();
                    if (deleteFilter == 3) {
                        if (!TextUtils.isEmpty(selection)) {
                            selection.append(" AND ");
                        }
                        selection.append("walnutTransactions.").append("flags & 16 = 0");
                    } else if (deleteFilter == 2) {
                        if (!TextUtils.isEmpty(selection)) {
                            selection.append(" AND ");
                        }
                        selection.append("walnutTransactions.").append("flags & 16 != 0");
                    }
                    if (!TextUtils.isEmpty(selection)) {
                        selection.append(" AND ");
                    }
                    selection.append("walnutTransactions.").append("flags & 256 != 0");
                } else {
                    if (TextUtils.equals(parentField, "pattern_UID")) {
                        int filter = matchingCriteria.getDeletedFilter();
                        if (filter == 3) {
                            if (!TextUtils.isEmpty(selection)) {
                                selection.append(" AND ");
                            }
                            selection.append("patternUID").append(" !=?");
                            selectionArgs.add(String.valueOf(txn.getRule().getPatternUID()));
                        } else if (filter == 2) {
                            if (!TextUtils.isEmpty(selection)) {
                                selection.append(" AND ");
                            }
                            selection.append("patternUID").append(" =?");
                            selectionArgs.add(String.valueOf(txn.getRule().getPatternUID()));
                        }
                    } else {
                        if (TextUtils.equals(matchType, "contains")) {
                            if (!TextUtils.isEmpty(selection)) {
                                selection.append(" AND ");
                            }
                            selection.append(parentField).append(" LIKE '%").append(childField).append("%'");
                        } else {
                            if (TextUtils.equals(matchType, "exact")) {
                                if (!TextUtils.isEmpty(selection)) {
                                    selection.append(" AND ");
                                }
                                selection.append(parentField).append(" =? ");
                                selectionArgs.add(childField);
                            }
                        }
                    }
                }
            }
        }
        SQLiteQueryBuilder sqb = new SQLiteQueryBuilder();
        sqb.setTables("walnutTransactions JOIN walnutAccounts ON walnutTransactions.accountId = walnutAccounts._id JOIN walnutSms ON walnutSms._id = walnutTransactions.WalnutSmsId");
        Cursor cursor = null;
        try {
            cursor = sqb.query(this.database, this.chainMatchingColumns, selection.toString(), (String[]) selectionArgs.toArray(new String[selectionArgs.size()]), null, null, "walnutTransactions._id DESC");
        } catch (RuntimeException exception) {
            Log.e(TAG, exception.toString());
        }
        Transaction oldTxn = null;
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                oldTxn = cursorToTransaction(cursor);
                oldTxn.setPanNo(cursor.getString(cursor.getColumnIndexOrThrow("pan")));
                if ((oldTxn.getFlags() & 16) == 0 && deleteFilter == 1) {
                    ShortSms sms = this.dbHelper.getSmsById(oldTxn.getSmsId());
                    while (sms.getSmsPreviousUUID() != null) {
                        sms = this.dbHelper.getSmsByUUID(sms.getSmsPreviousUUID());
                        oldTxn = getTransactionBySmsID(sms.get_id());
                        if ((oldTxn.getFlags() & 16) != 0) {
                            break;
                        }
                    }
                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return oldTxn;
    }

    private Transaction getTransactionBySmsID(long smsID) {
        Transaction txn = null;
        Cursor cursor = this.database.query("walnutTransactions", allColumns, "WalnutSmsId = " + smsID, null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                txn = cursorToTransaction(cursor);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return txn;
    }

    private String getTransactionDBField(String parentField) {
        int obj = -1;
        switch (parentField.hashCode()) {
            case -1413853096:
                if (parentField.equals("amount")) {
                    obj = 0;
                    break;
                }
                break;
            case 110749:
                if (parentField.equals("pan")) {
                    obj = 4;
                    break;
                }
                break;
            case 111188:
                if (parentField.equals("pos")) {
                    obj = 2;
                    break;
                }
                break;
            case 3076014:
                if (parentField.equals("date")) {
                    obj = 3;
                    break;
                }
                break;
            case 3387378:
                if (parentField.equals("note")) {
                    obj = 1;
                    break;
                }
                break;
        }
        switch (obj) {
            case 0 /*0*/:
                return "amount";
            case 1/*1*/:
                return "txnNote";
            case 2 /*2*/:
                return "pos";
            case 3 /*3*/:
                return "txnDate";
            case 4 /*4*/:
                return "pan";
            default:
                return parentField;
        }
    }

    private String getTransactionFieldValue(String parentField, Transaction txn) {
        String value = txn.getFieldValue(parentField);
        return value != null ? value : parentField;
    }

}
