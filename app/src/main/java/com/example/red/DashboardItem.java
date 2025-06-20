package com.example.red;

public class DashboardItem {
    private final String title;
    private final String iconUrl;
    private final int menuId;

    public DashboardItem(String title, String iconUrl, int menuId) {
        this.title = title;
        this.iconUrl = iconUrl;
        this.menuId = menuId;
    }

    public String getTitle() {
        return title;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public int getMenuId() {
        return menuId;
    }
}
