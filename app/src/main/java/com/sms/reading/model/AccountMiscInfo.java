package com.sms.reading.model;

import java.util.ArrayList;
import java.util.List;

public class AccountMiscInfo {
    private List<GetBalanceInfo> getBalanceInfo;

    public void setGetBalanceInfo(List<GetBalanceInfo> getBalanceInfo) {
        this.getBalanceInfo = getBalanceInfo;
    }

    public GetBalanceInfo getGetBalanceInfo(int accountType) {
        if (this.getBalanceInfo == null) {
            return null;
        }
        if (accountType == 1) {
            accountType = 2;
        }
        for (GetBalanceInfo getBalInfo : this.getBalanceInfo) {
            if (getBalInfo.getAccountType() == accountType) {
                return getBalInfo;
            }
        }
        return null;
    }

    public static class BalanceContactInfo {
        private String balRefreshText;
        private String contactSmsFormat;
        private int contactType;
        private String[] contacts;

        public String getPrimaryContact() {
            if (this.contacts.length > 0) {
                return this.contacts[0];
            }
            return null;
        }

        public void setContact(String[] contacts) {
            this.contacts = contacts;
        }

        public int getContactType() {
            return this.contactType;
        }

        public void setContactType(int contactType) {
            this.contactType = contactType;
        }

        public String getContactSmsFormat() {
            return this.contactSmsFormat;
        }

        public void setContactSmsFormat(String contactSmsFormat) {
            this.contactSmsFormat = contactSmsFormat;
        }

        public String getBalRefreshText() {
            return this.balRefreshText;
        }

        public void setBalRefreshText(String balRefreshText) {
            this.balRefreshText = balRefreshText;
        }

        public int getContactTypeInt(String accountType) {
            if (accountType.equalsIgnoreCase("sms")) {
                return 0;
            }
            if (accountType.equalsIgnoreCase("voice")) {
                return 1;
            }
            return -1;
        }

        public String toString() {
            return "AccountMiscInfo{contacts='" + this.contacts + '\'' + ", contactType=" + this.contactType + ", contactSmsFormat='" + this.contactSmsFormat + '\'' + '}';
        }
    }

    public static class GetBalanceInfo {
        public List<BalanceContactInfo> balContactInfo;
        private int accountType;

        public int getAccountType() {
            return this.accountType;
        }

        public void setAccountType(int accountType) {
            this.accountType = accountType;
        }

        public void setBalContactInfo(ArrayList<BalanceContactInfo> balContactInfo) {
            this.balContactInfo = balContactInfo;
        }

        public BalanceContactInfo getBalanceContactInfo(int contactType) {
            if (this.balContactInfo == null) {
                return null;
            }
            for (BalanceContactInfo balContact : this.balContactInfo) {
                if (balContact.getContactType() == contactType) {
                    return balContact;
                }
            }
            return null;
        }

        public String toString() {
            return "AccountMiscInfo{accountType=" + this.accountType + ", balContactInfo=" + this.balContactInfo + '}';
        }
    }
}
