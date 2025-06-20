package com.example.red;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private static final String PREF_NAME = Constants.PREF_NAME;
    private static final String TOKEN_KEY = Constants.KEY_TOKEN;
    private SharedPreferences prefs;

    public TokenManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        prefs.edit().putString(TOKEN_KEY, token).apply();
    }

    public String getToken() {
        return prefs.getString(TOKEN_KEY, null);
    }

    public boolean isTokenValid() {
        return getToken() != null && !getToken().isEmpty();
    }

    public void clearToken() {
        prefs.edit().remove(TOKEN_KEY).apply();
    }
}
