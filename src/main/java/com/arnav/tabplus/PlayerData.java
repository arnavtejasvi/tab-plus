package com.arnav.tabplus;

public class PlayerData {
    public String note = "";
    public RelationshipTag tag = RelationshipTag.NONE;

    public boolean isEmpty() {
        return note.isEmpty() && tag == RelationshipTag.NONE;
    }
}
