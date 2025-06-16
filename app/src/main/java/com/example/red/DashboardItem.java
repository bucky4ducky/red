package com.example.red;

public class DashboardItem {
    private String title;
    private String icon;

    public DashboardItem(String title, String icon) {
        this.title = title;
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public String getIcon() {
        return icon;
    }
}
