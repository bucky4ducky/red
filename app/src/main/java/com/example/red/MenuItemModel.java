package com.example.red;

import java.io.Serializable;

public class MenuItemModel implements Serializable {
    private String title;
    private String iconUrl;

    public MenuItemModel(String title, String iconUrl) {
        this.title = title;
        this.iconUrl = iconUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
}
