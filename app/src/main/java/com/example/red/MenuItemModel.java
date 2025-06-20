package com.example.red;

import java.io.Serializable;


public class MenuItemModel {
    private final String id;
    private final String title;
    private final String iconUrl;
    private final int menuId;
    private final boolean requiresAuth;

    public MenuItemModel(String id, String title, String iconUrl, int menuId, boolean requiresAuth) {
        this.id = id;
        this.title = title;
        this.iconUrl = iconUrl;
        this.menuId = menuId;
        this.requiresAuth = requiresAuth;
    }

    // Getters only - immutable object
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getIconUrl() { return iconUrl; }
    public int getMenuId() { return menuId; }
    public boolean requiresAuth() { return requiresAuth; }
}