package com.example.red;

import java.io.Serializable;

public class DashboardItem implements Serializable {
    private String title;
    private int iconResId = 0; // For local drawable resources, 0 if not used
    private String iconUrl = null; // For URL icons, null if not used
    private int menuId; // Associated menuId for click handling

    // Constructor for local drawable icons (e.g., for the 2x2 grid with ripple)
    public DashboardItem(String title, int iconResId, int menuId) {
        this.title = title;
        this.iconResId = iconResId;
        this.menuId = menuId;
    }

    // Constructor for URL-based icons (e.g., if loaded from API)
    public DashboardItem(String title, String iconUrl, int menuId) {
        this.title = title;
        this.iconUrl = iconUrl;
        this.menuId = menuId;
    }

    public String getTitle() {
        return title;
    }

    public int getIconResId() {
        return iconResId;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public int getMenuId() {
        return menuId;
    }

    // You can add setters if these properties need to be mutable
    public void setTitle(String title) {
        this.title = title;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
        this.iconUrl = null; // Clear URL if setting local resource
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
        this.iconResId = 0; // Clear local resource if setting URL
    }

    public void setMenuId(int menuId) {
        this.menuId = menuId;
    }
}
