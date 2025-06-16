package com.example.red;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class ApiRequest {
    private static ApiRequest instance;
    private RequestQueue requestQueue;

    private ApiRequest(Context context) {
        requestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }

    public static synchronized ApiRequest getInstance(Context context) {
        if (instance == null) {
            instance = new ApiRequest(context);
        }
        return instance;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        requestQueue.add(req);
    }

    public void cancelAll() {
        requestQueue.cancelAll(tag -> true);
    }
}