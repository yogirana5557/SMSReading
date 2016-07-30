package com.sms.reading.model;

import java.util.ArrayList;

public class ChainingRule {
    ParentMatchStatus parentMatch;
    ParentMatchStatus parentNoMatch;
    ArrayList<MatchingCriteria> parentSelection;

    public ArrayList<MatchingCriteria> getParentSelection() {
        return this.parentSelection;
    }

    public void updateParentSelections(MatchingCriteria matchingCriteria) {
        if (this.parentSelection == null) {
            this.parentSelection = new ArrayList();
        }
        this.parentSelection.add(matchingCriteria);
    }

    public ParentMatchStatus getParentMatch() {
        return this.parentMatch;
    }

    public void setParentMatch(ParentMatchStatus parentMatch) {
        this.parentMatch = parentMatch;
    }

    public ParentMatchStatus getParentNoMatch() {
        return this.parentNoMatch;
    }

    public void setParentNoMatch(ParentMatchStatus parentNoMatch) {
        this.parentNoMatch = parentNoMatch;
    }

    public static class MatchingCriteria {
        private String childField;
        private int deletedFilter;
        private String matchType;
        private long matchValue;
        private boolean overrideDeleted;
        private boolean overrideIncomplete;
        private String parentField;

        public boolean isOverrideIncomplete() {
            return this.overrideIncomplete;
        }

        public void setOverrideIncomplete(boolean overrideIncomplete) {
            this.overrideIncomplete = overrideIncomplete;
        }

        public long getMatchValue() {
            return this.matchValue;
        }

        public void setMatchValue(long matchValue) {
            this.matchValue = matchValue;
        }

        public int getDeletedFilter() {
            return this.deletedFilter;
        }

        public void setDeletedFilter(int deletedFilter) {
            this.deletedFilter = deletedFilter;
        }

        public boolean isOverrideDeleted() {
            return this.overrideDeleted;
        }

        public void setOverrideDeleted(boolean overrideDeleted) {
            this.overrideDeleted = overrideDeleted;
        }

        public String getParentField() {
            return this.parentField;
        }

        public void setParentField(String parentField) {
            this.parentField = parentField;
        }

        public String getChildField() {
            return this.childField;
        }

        public void setChildField(String childField) {
            this.childField = childField;
        }

        public String getMatchType() {
            return this.matchType;
        }

        public void setMatchType(String matchType) {
            this.matchType = matchType;
        }
    }

    public static class ParentMatchStatus {
        ArrayList<MatchingCriteria> child_override;
        ArrayList<MatchingCriteria> parent_override;

        public ArrayList<MatchingCriteria> getParentOverride() {
            return this.parent_override;
        }

        public void updateParentOverride(MatchingCriteria matchingCriteria) {
            if (this.parent_override == null) {
                this.parent_override = new ArrayList();
            }
            this.parent_override.add(matchingCriteria);
        }

        public ArrayList<MatchingCriteria> getChildOverride() {
            return this.child_override;
        }

        public void updateChildOverride(MatchingCriteria matchingCriteria) {
            if (this.child_override == null) {
                this.child_override = new ArrayList();
            }
            this.child_override.add(matchingCriteria);
        }
    }
}
