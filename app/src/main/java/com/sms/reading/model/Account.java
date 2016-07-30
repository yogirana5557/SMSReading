package com.sms.reading.model;

import android.text.TextUtils;

import com.sms.reading.BuildConfig;
import com.sms.reading.SMSApplication;

import java.util.ArrayList;
import java.util.HashMap;

public class Account {
    private static final String TAG;

    static {
        TAG = Account.class.getSimpleName();
    }

    private int _id;
    private AccountBalance balance;
    private int billCycleDay;
    private int billType;
    private String cardIssuer;
    private int colorIndex;
    private HashMap<Long, String> cycleMonthMap;
    private String displayName;
    private String displayPan;
    private boolean enabled;
    private int endDate;
    private int flags;
    private AccountMiscInfo miscInfo;
    private HashMap<Long, Double> monthTotalMap;
    private String muuid;
    private String name;
    private String pan;
    private HashMap<String, ArrayList<ShortSms>> smsMap;
    private int startDate;
    private HashMap<Long, Integer> txnCount;
    private int type;
    private long updatedDate;
    private String uuid;

    private Account() {
        this.colorIndex = -1;
    }

    public Account(String name, String pan, int type) {
        this.colorIndex = -1;
        this.smsMap = new HashMap();
        this.monthTotalMap = new HashMap();
        this.cycleMonthMap = new HashMap();
        this.txnCount = new HashMap();
        this.name = name;
        this.pan = pan;
        this.type = 99;
        this.billCycleDay = 1;
        this.type = type;
        this._id = -1;
    }

    public static int getAccountTypeInt(String accountType) {
        if (accountType.equalsIgnoreCase("debit_card")) {
            return 1;
        }
        if (accountType.equalsIgnoreCase("bank")) {
            return 2;
        }
        if (accountType.equalsIgnoreCase("credit_card")) {
            return 3;
        }
        if (accountType.equalsIgnoreCase("bill_pay")) {
            return 4;
        }
        if (accountType.equalsIgnoreCase("phone")) {
            return 5;
        }
        if (accountType.equalsIgnoreCase("generic")) {
            return 9;
        }
        if (accountType.equalsIgnoreCase("filter")) {
            return 10;
        }
        if (accountType.equalsIgnoreCase("placeholder")) {
            return 6;
        }
        if (accountType.equalsIgnoreCase("unknown")) {
            return 99;
        }
        if (accountType.equalsIgnoreCase("ignore")) {
            return 9999;
        }
        if (accountType.equalsIgnoreCase("prepaid")) {
            return 17;
        }
        return 99;
    }

    public static String getAccountType(int accType) {
        switch (accType) {
            case 1 /*1*/:
                return "debit_card";
            case 2 /*2*/:
                return "bank";
            case 3 /*3*/:
                return "credit_card";
            case 4 /*4*/:
                return "bill_pay";
            case 5 /*5*/:
                return "phone";
            case 6 /*6*/:
                return "placeholder";
            case 9 /*9*/:
                return "generic";
            case 10 /*10*/:
                return "filter";
            case 17 /*17*/:
                return "prepaid";
            case 9999:
                return "ignore";
            default:
                return "unknown";
        }
    }

    public static String getAccountTypeName(int accType) {
        switch (accType) {
            case 1 /*1*/:
            case 3 /*3*/:
                return "card";
            case 2 /*2*/:
                return "bank";
            default:
                return null;
        }
    }

    public static String getAccountNamePostfix(int transactionType) {
        switch (transactionType) {
            case 3 /*3*/:
                return "credit";
            default:
                return BuildConfig.VERSION_NAME;
        }
    }

    public static int getResourceId(Account account) {
        switch (account.getType()) {
            case 1 /*1*/:
            case 3 /*3*/:
                return 2130837693;
            case 2 /*2*/:
                return 2130837681;
            case 4 /*4*/:
            case 6 /*6*/:
                return 2130837853;
            case 5 /*5*/:
                return 2130837789;
            case 7 /*7*/:
                return 2130837697;
            case 8 /*8*/:
                return 2130837678;
            case 11 /*11*/:
                return 2130837787;
            case 12 /*12*/:
                return 2130837827;
            case 13 /*13*/:
                return 2130837853;
            case 17 /*17*/:
                return 2130837730;
            default:
                return 2130837693;
        }
    }


    public long getUpdatedDate() {
        return this.updatedDate;
    }

    public void setUpdatedDate(long updatedDate) {
        this.updatedDate = updatedDate;
    }

    public Integer getTxnCount(long month) {
        return (Integer) this.txnCount.get(Long.valueOf(month));
    }

    public void setTxnCount(long month, int txnCount) {
        this.txnCount.put(Long.valueOf(month), Integer.valueOf(txnCount));
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return TextUtils.isEmpty(this.displayName) ? this.name : this.displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getBillCycleDay() {
        return this.billCycleDay;
    }

    public void setBillCycleDay(int billCycleDay) {
        this.billCycleDay = billCycleDay;
    }

    public AccountBalance getBalanceInfo() {
        return this.balance;
    }

    public void setBalanceInfo(AccountBalance bal) {
        this.balance = bal;
    }

    public AccountMiscInfo getMiscInfo() {
        if (this.miscInfo == null) {
            this.miscInfo = SMSApplication.getInstance().getAccountMiscInfo(this);
        }
        return this.miscInfo;
    }

    public void addShortSmsList(ArrayList<ShortSms> smslist, String subcategory) {
        this.smsMap.put(subcategory, smslist);
    }

    public String getKey() {
        return this.name + this.pan;
    }

    public String toString() {
        return this.name;
    }

    public HashMap<String, ArrayList<ShortSms>> getSmsMap() {
        return this.smsMap;
    }

    public String getPan() {
        return this.pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public String getDisplayPan() {
        return TextUtils.isEmpty(this.displayPan) ? this.pan : this.displayPan;
    }

    public void setDisplayPan(String displayPan) {
        this.displayPan = displayPan;
    }

    public int get_id() {
        return this._id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public double getTotal(long month) {
        if (this.monthTotalMap.get(Long.valueOf(month)) != null) {
            return ((Double) this.monthTotalMap.get(Long.valueOf(month))).doubleValue();
        }
        return 0.0d;
    }

    public void setTotal(long month, double t) {
        this.monthTotalMap.put(Long.valueOf(month), Double.valueOf(t));
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getCycleMonth(long month) {
        return (String) this.cycleMonthMap.get(Long.valueOf(month));
    }

    public void setCycleMonth(long month, String cycleMonth) {
        this.cycleMonthMap.put(Long.valueOf(month), cycleMonth);
    }

    public HashMap<Long, String> getCycleMonthMap() {
        return this.cycleMonthMap;
    }

    public void setCycleMonthMap(HashMap<Long, String> cycleMonthMap) {
        this.cycleMonthMap = cycleMonthMap;
    }

    public int getStartDate() {
        return this.startDate;
    }

    public void setStartDate(int startDate) {
        this.startDate = startDate;
    }

    public int getEndDate() {
        return this.endDate;
    }

    public void setEndDate(int endDate) {
        this.endDate = endDate;
    }

    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getMUUID() {
        return this.muuid;
    }

    public void setMUUID(String uuid) {
        this.muuid = uuid;
    }

    public int getColorIndex() {
        return this.colorIndex;
    }

    public void setColorIndex(int color) {
        this.colorIndex = color;
    }

    public String getCardIssuer() {
        return this.cardIssuer;
    }

    public void setCardIssuer(String cardIssuer) {
        this.cardIssuer = cardIssuer;
    }

    public int getBillType() {
        return this.billType;
    }

    public void setBillType(int billType) {
        this.billType = billType;
    }

    public int getFlags() {
        return this.flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public boolean isBalanceInfoAccount() {
        return this.type == 3 || this.type == 2 || this.type == 1 || this.type == 17;
    }

    public boolean isSuggestedAccount() {
        return this.type == 1 || this.type == 2;
    }

    public boolean isBalUptoDate() {
        return (this.flags & 4) != 4;
    }

    public boolean isOutbalUptoDate() {
        return (this.flags & 8) != 8;
    }

    public boolean isNotAnExpenseAccount() {
        return (this.flags & 16) == 16;
    }

    public void setAsNotAnExpenseAccount() {
        this.flags |= 16;
    }

    public void setAsExpenseAccount() {
        this.flags &= -17;
    }

    public String getFormatedPan() {
        if (this.type == 3 || this.type == 1) {
            if (TextUtils.isDigitsOnly(this.displayPan)) {
                return "XXX-XXX-XXX-" + this.displayPan;
            }
        } else if (this.type == 2 && TextUtils.isDigitsOnly(this.displayPan)) {
            return "XXXXXX" + this.displayPan;
        }
        return this.displayPan;
    }

    public String getFormatedDisplayPan(String panStr) {
        if (!TextUtils.isEmpty(panStr)) {
            String pan;
            if (this.type == 3 || this.type == 1) {
                pan = panStr.replace("XXX-XXX-XXX-", BuildConfig.VERSION_NAME);
                if (TextUtils.isDigitsOnly(pan)) {
                    return pan;
                }
            } else if (this.type == 2) {
                pan = panStr.replace("XXXXXX", BuildConfig.VERSION_NAME);
                if (TextUtils.isDigitsOnly(pan)) {
                    return pan;
                }
            }
        }
        return panStr;
    }
}
