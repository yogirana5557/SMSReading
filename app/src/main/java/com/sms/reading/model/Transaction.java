package com.sms.reading.model;

import android.content.Context;
import android.location.Location;
import android.text.TextUtils;
import android.util.Log;

import com.sms.reading.db.DBHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class Transaction extends ShortSms {
    private static final String TAG;

    static {
        TAG = Transaction.class.getSimpleName();
    }

    private String UUID;
    private AccountBalance accountBalance;
    private double amount;
    private String displayPan;
    private int flags;
    private long merchantId;
    private String pan;
    private String placeId;
    private Location placeLocation;
    private String placeName;
    private String pos;
    private String txnCategories;
    private Date txnDate;
    private String txnNote;
    private String txnPhoto;
    private String txnPhotoServerPath;
    private String txnTags;
    private int txnType;

    public Transaction(String number, String body, Date date) {
        super(number, body, date);
    }

    public static int[] getAllTypes() {
        return new int[]{1, 2, 3, 4, 6, 5, 7, 10, 9, 13, 14, 15, 16};
    }

    public static int[] getAllSpendsTypes() {
        return new int[]{1, 2, 4, 6, 5, 7, 10, 9, 13, 14, 16};
    }

    public static int[] getBillTxnTypes() {
        return new int[]{5, 6};
    }

    public static boolean isBillTxnTypes(int txnType) {
        return txnType == 5 || txnType == 6;
    }

    public static int[] getNotExpenseTxnTypes() {
        return new int[]{5, 4, 10, 9, 13};
    }

    public static boolean isNotExpenseTxnTypes(int txnType) {
        return txnType == 4 || txnType == 5 || txnType == 10 || txnType == 9 || txnType == 13;
    }

    public static int[] getAllCashSpendTypes() {
        return new int[]{7};
    }

    public static int[] getAllATMTypes() {
        return new int[]{3, 15};
    }

    public static int[] getAllCardSwipeTypes() {
        return new int[]{1, 2};
    }

    public static int[] getAllCardSwipeTypesIncludePrepaid() {
        return new int[]{1, 2, 16};
    }

    public static boolean isMerchantCardType(int cardType) {
        return cardType == 1 || cardType == 2 || cardType == 7 || cardType == 14;
    }

    public static boolean isSpendType(int cardType) {
        return cardType == 1 || cardType == 2 || cardType == 3 || cardType == 4 || cardType == 6 || cardType == 10 || cardType == 5 || cardType == 7 || cardType == 9 || cardType == 13 || cardType == 14 || cardType == 15 || cardType == 16;
    }

    public static int getTransactionTypeInt(String transactionType) {
        if (transactionType == null) {
            return 9;
        }
        if (transactionType.equalsIgnoreCase("credit_card")) {
            return 1;
        }
        if (transactionType.equalsIgnoreCase("debit_card")) {
            return 2;
        }
        if (transactionType.equalsIgnoreCase("debit_atm")) {
            return 3;
        }
        if (transactionType.equalsIgnoreCase("net_banking")) {
            return 4;
        }
        if (transactionType.equalsIgnoreCase("bill_pay")) {
            return 5;
        }
        if (transactionType.equalsIgnoreCase("ecs")) {
            return 6;
        }
        if (transactionType.equalsIgnoreCase("cheque")) {
            return 10;
        }
        if (transactionType.equalsIgnoreCase("default")) {
            return 9;
        }
        if (transactionType.equalsIgnoreCase("balance")) {
            return 12;
        }
        if (transactionType.equalsIgnoreCase("credit")) {
            return 17;
        }
        if (transactionType.equalsIgnoreCase("credit_atm")) {
            return 15;
        }
        if (transactionType.equalsIgnoreCase("debit_prepaid")) {
            return 16;
        }
        return 9;
    }

    public static String getAccountNamePostfix(int transactionType) {
        switch (transactionType) {
            case 1 /*1*/:
                return "credit";
            case 2 /*2*/:
                return "debit";
            case 3 /*3*/:
                return "debit";
            case 4 /*4*/:
                return "";
            case 5/*5*/:
                return "";
            case 6 /*6*/:
                return "";
            case 10 /*10*/:
                return "";
            case 15 /*15*/:
                return "credit";
            default:
                return "";
        }
    }

    public void setTransaction(String pan, Double amount, Date txnDate, String pos, int txnType) {
        this.amount = amount.doubleValue();
        this.txnDate = txnDate;
        this.txnType = txnType;
        this.pan = pan;
        this.pos = pos;
        this.merchantId = -1;
        this.txnCategories = "other";
    }

    public void setNoPos() {
        this.flags |= 1;
    }

    public boolean hasPos() {
        return (this.flags & 1) != 1;
    }

    public void setNoPan() {
        this.flags |= 2;
    }

    public void setHasAccurateDate() {
        this.flags |= 4;
    }

    public boolean hasAccurateDate() {
        return (this.flags & 4) == 4;
    }

    public boolean isNotAnExpense() {
        return (this.flags & 32) == 32;
    }

    public void setIsNotAnExpense() {
        this.flags |= 32;
    }

    public void setIsExpense() {
        this.flags &= -33;
    }

    public int getFlags() {
        return this.flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public boolean isTxnSplit() {
        return (this.flags & 64) == 64;
    }

    public void setIsTxnSplit(boolean isTxnSplit) {
        if (isTxnSplit) {
            this.flags |= 64;
        } else {
            this.flags &= -65;
        }
    }

    public boolean isTxnPayment() {
        return (this.flags & 512) == 512;
    }

    public void setTxnPayment(boolean isTxnPayment) {
        if (isTxnPayment) {
            this.flags |= 512;
        } else {
            this.flags &= -513;
        }
    }

    public boolean isDuplicate() {
        return (this.flags & 128) == 128;
    }

    public void setDuplicate(boolean duplicate) {
        if (duplicate) {
            this.flags |= 128;
        } else {
            this.flags &= -129;
        }
    }

    public String getPanNo() {
        return this.pan;
    }

    public void setPanNo(String p) {
        this.pan = p;
    }

    public String getDisplayPan() {
        return TextUtils.isEmpty(this.displayPan) ? this.pan : this.displayPan;
    }

    public void setDisplayPan(String displayPan) {
        this.displayPan = displayPan;
    }

    public String getPos() {
        return this.pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    public double getAmount() {
        return this.amount;
    }

    public void setAmount(double amnt) {
        this.amount = amnt;
    }

    public AccountBalance getBalance() {
        return this.accountBalance;
    }

    public void setBalance(AccountBalance accountBalance) {
        this.accountBalance = accountBalance;
    }

    public Date getTxnDate() {
        return this.txnDate;
    }

    public void setTxnDate(Date txnDate) {
        this.txnDate = txnDate;
    }

    public int getTxnType() {
        return this.txnType;
    }

    public void setTxnType(int tType) {
        this.txnType = tType;
    }

    public String getTxnNote() {
        return this.txnNote;
    }

    public void setTxnNote(String note) {
        this.txnNote = note;
    }

    public String getTxnPhoto() {
        return this.txnPhoto;
    }

    public void setTxnPhoto(String path) {
        this.txnPhoto = path;
    }

    public String getTxnPhotoServerPath() {
        return this.txnPhotoServerPath;
    }

    public void setTxnPhotoServerPath(String txnPhotoServerPath) {
        this.txnPhotoServerPath = txnPhotoServerPath;
    }

    public boolean isNotCategorised() {
        return TextUtils.isEmpty(getTxnCategories()) || getTxnCategories().equals("other");
    }

    public String getUUID() {
        return this.UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(super.toString());
        builder.append("\npan : " + this.pan);
        builder.append("\npos : " + this.pos);
        builder.append("\namount : " + this.amount);
        builder.append("\ntxnType : " + this.txnType);
        builder.append("\ntxnCategories : " + this.txnCategories);
        builder.append("\ntxnTags : " + this.txnTags);
        builder.append("\nplaceId : " + this.placeId);
        builder.append("\nplaceName : " + this.placeName);
        builder.append("\nbal : " + this.accountBalance);
        builder.append("\ndisplayPan : " + this.displayPan);
        builder.append("\naccountDisplayName : " + this.accountDisplayName);
        builder.append("\naccountType : " + this.accountType);
        return builder.toString();
    }

    public long getMerchantId() {
        return this.merchantId;
    }

    public void setMerchantId(long merchantId) {
        this.merchantId = merchantId;
    }

    public String getPlaceId() {
        return this.placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getPlaceName() {
        return this.placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public Location getPlaceLocation() {
        return this.placeLocation;
    }

    public void setPlaceLocation(Location placeLocation) {
        this.placeLocation = placeLocation;
    }

    public String getTxnCategories() {
        return this.txnCategories;
    }

    public void setTxnCategories(String categories) {
        this.txnCategories = categories;
    }

    public String getTxnTags() {
        return this.txnTags;
    }

    public void setTxnTags(String tags) {
        this.txnTags = tags;
    }

    public boolean updatefromTranscation(Context context, DBHelper dbHelper, Transaction otherTxn) {
        if (otherTxn == null) {
            return false;
        }
        String onlineTag = "online tag";
        if (otherTxn.getTxnTags() != null && otherTxn.getTxnTags().contains(onlineTag)) {
            setTxnTags(onlineTag);
        }
        setTxnCategories(otherTxn.getTxnCategories());
        setPlaceId(otherTxn.getPlaceId());
        setPlaceName(otherTxn.getPlaceName());
        setPlaceLocation(otherTxn.getPlaceLocation());
        setMerchantId(otherTxn.getMerchantId());
        dbHelper.updateTransactionMerchant(this);
        return true;
    }

    public boolean updateCategoryfromTranscation(Context context, DBHelper dbHelper, Transaction otherTxn) {
        if (otherTxn == null) {
            return false;
        }
        String onlineTag = "online tag";
        if (otherTxn.getTxnTags() != null && otherTxn.getTxnTags().contains(onlineTag)) {
            setTxnTags(onlineTag);
        }
        setTxnCategories(otherTxn.getTxnCategories());
        setPlaceName(otherTxn.getPlaceName());
        dbHelper.updateTransactionMerchant(this);
        return true;
    }

    public Transaction findSimilarTxnAndUpdate(Context context, DBHelper dbhelper) {
        Transaction txnClosestSimilar = null;
        Transaction txnLatestSimilar = null;
        float closestDistance = Float.MAX_VALUE;
        float[] results = new float[3];
        String onlineTag = "online tag";
        ArrayList<ShortSms> txnlist = dbhelper.getTransactions(null, null, getPos(), null, null, false);
        StringBuilder log = new StringBuilder();
        log.append("Similar search - POS : ").append(getPos()).append("|Found ").append(txnlist.size());
        Iterator it = txnlist.iterator();
        while (it.hasNext()) {
            Transaction txnSimilar = (Transaction) ((ShortSms) it.next());
            if (!(txnSimilar.isTxnPayment() || get_id() == txnSimilar.get_id())) {
                if (txnSimilar.getTxnTags() != null && txnSimilar.getTxnTags().contains(onlineTag)) {
                    log.append("|Matched local online txn ").append(txnSimilar.getPos()).append("/").append(txnSimilar.getTxnCategories());
                    Log.d("CatDBG", log.toString());
                    updatefromTranscation(context, dbhelper, txnSimilar);
                    return txnSimilar;
                } else if (!(txnSimilar.getTxnCategories() == null || txnSimilar.isNotCategorised())) {
                    Location locSimilar = txnSimilar.getLocation();
                    Location locCurrent = getLocation();
                    if (locCurrent == null) {
                        log.append("|No loc, matched local txn ").append(txnSimilar.getPos()).append("/").append(txnSimilar.getTxnCategories());
                        Log.d("CatDBG", log.toString());
                        updateCategoryfromTranscation(context, dbhelper, txnSimilar);
                        return txnSimilar;
                    } else if (locSimilar != null) {
                        Location.distanceBetween(locSimilar.getLatitude(), locSimilar.getLongitude(), locCurrent.getLatitude(), locCurrent.getLongitude(), results);
                        float distance = results[0];
                        if (distance <= 2000.0f) {
                            log.append("|matched local txn with loc ").append(txnSimilar.getPos());
                            Log.d("CatDBG", log.toString());
                            updatefromTranscation(context, dbhelper, txnSimilar);
                            return txnSimilar;
                        } else if (distance < closestDistance) {
                            closestDistance = distance;
                            txnClosestSimilar = txnSimilar;
                        }
                    } else if (txnLatestSimilar == null) {
                        txnLatestSimilar = txnSimilar;
                    }
                }
            }
        }
        if (txnClosestSimilar != null) {
            log.append("|closest matched local txn ").append(txnClosestSimilar.getPos()).append("/").append(txnClosestSimilar.getTxnCategories());
            updateCategoryfromTranscation(context, dbhelper, txnClosestSimilar);
        } else if (txnLatestSimilar != null) {
            log.append("|latest matched local txn ").append(txnLatestSimilar.getPos()).append("/").append(txnLatestSimilar.getTxnCategories());
            updateCategoryfromTranscation(context, dbhelper, txnLatestSimilar);
        }
        Log.d("CatDBG", log.toString());
        return txnClosestSimilar;
    }

    public String getFieldValue(String field) {
        int obj = -1;
        switch (field.hashCode()) {
            case -1413853096:
                if (field.equals("amount")) {
                    obj = 0;
                    break;
                }
                break;
            case 110749:
                if (field.equals("pan")) {
                    obj = 3;
                    break;
                }
                break;
            case 111188:
                if (field.equals("pos")) {
                    obj = 2;
                    break;
                }
                break;
            case 3076014:
                if (field.equals("date")) {
                    obj = 5;
                    break;
                }
                break;
            case 3387378:
                if (field.equals("note")) {
                    obj = 1;
                    break;
                }
                break;
            case 509054971:
                if (field.equals("transaction_type")) {
                    obj = 4;
                    break;
                }
                break;
        }
        switch (obj) {
            case 0 /*0*/:
                return String.valueOf(getAmount());
            case 1 /*1*/:
                return getTxnNote();
            case 2 /*2*/:
                return getPos();
            case 3 /*3*/:
                return String.valueOf(getPanNo());
            case 4 /*4*/:
                return String.valueOf(getTxnType());
            case 5 /*5*/:
                return String.valueOf(getTxnDate().getTime());
            default:
                return null;
        }
    }

}
