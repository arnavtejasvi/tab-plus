package com.arnav.tabplus;

public enum RelationshipTag {
    NONE(0xFFAAAAAA, ""),
    FRIEND(0xFF55FF55, "Friend"),
    ALLY(0xFF5599FF, "Ally"),
    RIVAL(0xFFFFAA00, "Rival"),
    ENEMY(0xFFFF5555, "Enemy");

    public final int color;
    public final String label;

    RelationshipTag(int color, String label) {
        this.color = color;
        this.label = label;
    }

    public RelationshipTag next() {
        RelationshipTag[] vals = values();
        return vals[(ordinal() + 1) % vals.length];
    }
}
