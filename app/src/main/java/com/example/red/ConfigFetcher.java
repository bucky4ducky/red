package com.example.red;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import android.util.Log;


public class ConfigFetcher {
    public static void fetchappconfiguration(Context context){
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                Constants.data_api,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            Gson gson = new Gson();
                            ApiResponse apiResponse = gson.fromJson(response, ApiResponse.class);
                            ConfigData configdata = apiResponse.getdata();

                            if (configdata != null) {
                                storage.getInstance().setConfigData(configdata);
                                Log.d("ConfigFetcher", "App config loaded " + configdata.getAppName());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
                      queue.add(stringRequest);
    }
}
