package com.example.red;

import androidx.annotation.NonNull;

public class storage {

    private static storage instance;
    private ConfigData configData;
    private storage(){
    }


    public static synchronized storage getInstance(){
        if (instance == null){
            instance = new storage();
        }
        return instance;
    }

    public ConfigData getConfigData() {
        return configData;
    }

    public void setConfigData(ConfigData configData) {
        this.configData = configData;
    }

}















