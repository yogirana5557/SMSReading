package com.sms.reading.model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AccountBalance {
    private double balance;
    private Date lastBalSyncdate;
    private Date lastOutbalSyncdate;
    private double outstandingBalance;

    public static boolean isLatest(Date newDate, Date existingDate) {
        if (newDate == null || existingDate == null || newDate.equals(existingDate) || newDate.after(existingDate)) {
            return true;
        }
        return false;
    }

    public static String getBalanceDateFormat(Date date) {
        if (date != null) {
            return new SimpleDateFormat("dd''MMM").format(date);
        }
        return null;
    }

    public Date getBalSyncDate() {
        return this.lastBalSyncdate;
    }

    public void setBalSyncDate(Date lastSyncdate) {
        this.lastBalSyncdate = lastSyncdate;
    }

    public Date getOutbalSyncdate() {
        return this.lastOutbalSyncdate;
    }

    public void setOutbalSyncdate(Date lastOutbalSyncdate) {
        this.lastOutbalSyncdate = lastOutbalSyncdate;
    }

    public double getBalance() {
        return this.balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public double getOutstandingBalance() {
        return this.outstandingBalance;
    }

    public void setOutstandingBalance(double outstandingBalance) {
        this.outstandingBalance = outstandingBalance;
    }

    public String toString() {
        return "AccountBalance{balance=" + this.balance + ", outstandingBalance=" + this.outstandingBalance + ", lastBalSyncdate=" + this.lastBalSyncdate + ", lastOutbalSyncdate=" + this.lastOutbalSyncdate + '}';
    }
}
