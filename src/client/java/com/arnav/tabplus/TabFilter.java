package com.arnav.tabplus;

import org.jetbrains.annotations.Nullable;

public class TabFilter {
    private static final TabFilter INSTANCE = new TabFilter();

    private final StringBuilder filter = new StringBuilder();
    private int selectedIndex = 0;
    private int listSize = 0;
    private boolean openProfile = false;

    private TabFilter() {}

    public static TabFilter getInstance() {
        return INSTANCE;
    }

    public String getFilter() {
        return filter.toString();
    }

    public void appendChar(char c) {
        filter.append(c);
        selectedIndex = 0;
    }

    public void backspace() {
        if (!filter.isEmpty()) {
            filter.deleteCharAt(filter.length() - 1);
            selectedIndex = 0;
        }
    }

    public void clear() {
        filter.setLength(0);
        selectedIndex = 0;
        listSize = 0;
        openProfile = false;
    }

    public void moveSelection(int delta) {
        if (listSize <= 0) return;
        selectedIndex = Math.floorMod(selectedIndex + delta, listSize);
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setListSize(int size) {
        this.listSize = size;
        if (selectedIndex >= size) selectedIndex = Math.max(0, size - 1);
    }

    public void requestOpenProfile() {
        openProfile = true;
    }

    public boolean consumeOpenProfile() {
        boolean val = openProfile;
        openProfile = false;
        return val;
    }
}
