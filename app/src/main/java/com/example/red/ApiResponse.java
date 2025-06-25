package com.example.red;




public class ApiResponse {
    private boolean status;
    private String message;
    private ConfigData data;

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public ConfigData getdata() {
        return data;
    }//working

    public void setdata(ConfigData data) {
        this.data = data;
    }
}