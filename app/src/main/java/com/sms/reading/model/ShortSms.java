package com.sms.reading.model;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class ShortSms implements Parcelable {
    public static final Creator<ShortSms> CREATOR;

    static {
        CREATOR = new C06211();
    }

    long _id;
    int accountColorIndex;
    String accountDisplayName;
    boolean accountEnabled;
    int accountId;
    String accountName;
    String accountOverrideName;
    int accountType;
    String body;
    String category;
    ChainingRule chainingRule;
    Date date;
    boolean isChainingEnabled;
    Location location;
    String number;
    boolean parsed;
    boolean showNotification;
    int smsFlag;
    long smsId;
    String smsPreviousUUID;
    int smsType;
    String smsUUID;
    String subCategory;
    private boolean isExpenseAcc;
    private Rule rule;

    public ShortSms() {
        this.accountEnabled = true;
        this.isExpenseAcc = true;
        this.showNotification = true;
    }

    public ShortSms(String number, String body, Date date) {
        this.accountEnabled = true;
        this.isExpenseAcc = true;
        this.showNotification = true;
        this.number = number;
        this.body = body;
        this.date = date;
        this.smsId = -1;
        this._id = -1;
        this.smsType = 99;
        this.accountType = 99;
        this.accountId = -1;
        this.parsed = false;
    }

    public ShortSms(Parcel in) {
        this.accountEnabled = true;
        this.isExpenseAcc = true;
        this.showNotification = true;
        this._id = in.readLong();
        this.smsId = in.readLong();
        this.number = in.readString();
        this.body = in.readString();
        this.date = new Date(in.readLong());
        this.location = new Location("WalnutLocation");
        this.location.setLatitude(in.readDouble());
        this.location.setLongitude(in.readDouble());
        this.location.setAccuracy(in.readFloat());
    }


    public void setCategories(String category, String subCategory) {
        this.category = category;
        this.subCategory = subCategory;
    }

    public String getCategory() {
        return this.category;
    }

    public int getSmsType() {
        return this.smsType;
    }

    public void setSmsType(int smsType) {
        this.smsType = smsType;
    }

    public Date getDate() {
        return this.date;
    }

    public void setDate(Date d) {
        this.date = d;
    }

    public int getAccountType() {
        return this.accountType;
    }

    public void setAccountType(int accountType) {
        this.accountType = accountType;
    }

    public String getNumber() {
        return this.number;
    }

    public String getBody() {
        return this.body;
    }

    public long get_id() {
        return this._id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public Location getLocation() {
        return this.location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public int getAccountId() {
        return this.accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public String getAccountDisplayName() {
        return TextUtils.isEmpty(this.accountDisplayName) ? this.accountName : this.accountDisplayName;
    }

    public void setAccountDisplayName(String accountDisplayName) {
        this.accountDisplayName = accountDisplayName;
    }

    public String getAccountName() {
        return this.accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public long getSmsId() {
        return this.smsId;
    }

    public void setSmsId(long smsId) {
        this.smsId = smsId;
    }

    public boolean isParsed() {
        return this.parsed;
    }

    public void setParsed(boolean parsed) {
        this.parsed = parsed;
    }

    public Rule getRule() {
        return this.rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    public int getSmsFlag() {
        return this.smsFlag;
    }

    public void setSmsFlag(int smsFlag) {
        this.smsFlag = smsFlag;
    }

    public String getSmsUUID() {
        return this.smsUUID;
    }

    public void setSmsUUID(String smsUUID) {
        this.smsUUID = smsUUID;
    }

    public String getSmsPreviousUUID() {
        return this.smsPreviousUUID;
    }

    public void setSmsPreviousUUID(String smsPreviousUUID) {
        this.smsPreviousUUID = smsPreviousUUID;
    }

    public ChainingRule getChainingRule() {
        return this.chainingRule;
    }

    public void setChainingRule(ChainingRule chainingRule) {
        this.chainingRule = chainingRule;
    }

    public boolean isChainingEnabled() {
        return this.isChainingEnabled;
    }

    public void setIsChainingEnabled(boolean isChainingEnabled) {
        this.isChainingEnabled = isChainingEnabled;
    }

    public int getAccountColorIndex() {
        return this.accountColorIndex;
    }

    public void setAccountColorIndex(int accountColorIndex) {
        this.accountColorIndex = accountColorIndex;
    }

    public boolean showNotification() {
        return this.showNotification;
    }

    public void setShowNotification(boolean showNotification) {
        this.showNotification = showNotification;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(this._id);
        parcel.writeLong(this.smsId);
        parcel.writeString(this.number);
        parcel.writeString(this.body);
        parcel.writeLong(this.date.getTime());
        if (this.location != null) {
            parcel.writeDouble(this.location.getLatitude());
            parcel.writeDouble(this.location.getLongitude());
            parcel.writeFloat(this.location.getAccuracy());
            return;
        }
        parcel.writeDouble(-1.0d);
        parcel.writeDouble(-1.0d);
        parcel.writeFloat(-1.0f);
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\nnumber :" + this.number);
        builder.append("\ndate :" + this.date);
        builder.append("\ncategory :" + this.category);
        builder.append("\nsubCategory :" + this.subCategory);
        builder.append("\nsmsType :" + this.smsType);
        builder.append("\naccountType :" + this.accountType);
        builder.append("\naccountId :" + this.accountId);
        builder.append("\nparsed :" + this.parsed);
        builder.append("\nbody :" + this.body);
        return builder.toString();
    }

    public ArrayList<ChainingRule.MatchingCriteria> getParentSelectionCriteriaList() {
        if (this.chainingRule != null) {
            return this.chainingRule.getParentSelection();
        }
        return null;
    }

    public boolean shouldDeleteChild() {
        if (this.chainingRule == null) {
            return false;
        }
        ChainingRule.ParentMatchStatus parentNoMatch = this.chainingRule.getParentNoMatch();
        if (parentNoMatch == null) {
            return false;
        }
        Iterator it = parentNoMatch.getChildOverride().iterator();
        while (it.hasNext()) {
            ChainingRule.MatchingCriteria selection = (ChainingRule.MatchingCriteria) it.next();
            if (selection.isOverrideDeleted()) {
                return selection.isOverrideDeleted();
            }
        }
        return false;
    }

    public boolean isAccountEnabled() {
        return this.accountEnabled;
    }

    public void setIsAccountEnabled(boolean enabled) {
        this.accountEnabled = enabled;
    }

    public boolean isExpenseAccount() {
        return this.isExpenseAcc;
    }

    public void setIsExpenseAcc(boolean isExpenseAcc) {
        this.isExpenseAcc = isExpenseAcc;
    }

    public String getAccountOverrideName() {
        return this.accountOverrideName;
    }

    public void setAccountOverrideName(String accountOverrideName) {
        this.accountOverrideName = accountOverrideName;
    }

    /* renamed from: com.daamitt.walnut.app.components.ShortSms.1 */
    static class C06211 implements Creator<ShortSms> {
        C06211() {
        }

        public ShortSms createFromParcel(Parcel in) {
            return new ShortSms(in);
        }

        public ShortSms[] newArray(int size) {
            return new ShortSms[size];
        }
    }
}
