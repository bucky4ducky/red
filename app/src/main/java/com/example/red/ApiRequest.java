package com.example.red;

import android.content.Context;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class ApiRequest {
    private static ApiRequest instance;
    private RequestQueue requestQueue;
    private static Context context;

    private ApiRequest(Context ctx) {
        context = ctx.getApplicationContext();
        requestQueue = Volley.newRequestQueue(context);
    }

    public static synchronized ApiRequest getInstance(Context context) {
        if (instance == null) {
            instance = new ApiRequest(context);
        }
        return instance;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        // Add authentication retry logic
        req.setRetryPolicy(new DefaultRetryPolicy(
                10000,  // 10 second timeout
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(req);
    }

    public void cancelAll() {
        requestQueue.cancelAll(tag -> true);
    }
}