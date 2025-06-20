package com.example.red;

import android.app.AlarmManager;
import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.AuthFailureError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ApiRequest {
    private static ApiRequest instance;
    private final RequestQueue requestQueue;
    private final SharedPreferences sharedPreferences;
    private final Context context;

    private ApiRequest(Context ctx) {
        this.context = ctx.getApplicationContext();
        this.requestQueue = Volley.newRequestQueue(context);
        this.sharedPreferences = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized ApiRequest getInstance(Context context) {
        if (instance == null) {
            instance = new ApiRequest(context);
        }
        return instance;
    }

    public void login(String employeeCode, String password, String appId, String deviceId,
                      Response.Listener<String> listener, Response.ErrorListener errorListener) {

        StringRequest request = new StringRequest(Request.Method.POST, Constants.LOGIN_URL,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        JSONObject data = json.getJSONObject("data");
                        String token = data.getString("token");

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(Constants.KEY_TOKEN, token);
                        editor.apply();

                        listener.onResponse(response);
                    } catch (JSONException e) {
                        errorListener.onErrorResponse(new VolleyError("Invalid login response"));
                    }
                },
                errorListener
        ) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                JSONObject json = new JSONObject();
                try {
                    json.put("EmployeeCode", employeeCode);
                    json.put("Password", password);
                    json.put("AppId", appId);
                    json.put("DeviceId", deviceId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return json.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("secret-key", Constants.SECRET_KEY);
                return headers;
            }
        };

        requestQueue.add(request);
    }

    public void getMenu(Response.Listener<String> listener, Response.ErrorListener errorListener) {
        StringRequest request = new StringRequest(Request.Method.POST, Constants.MENU_URL,
                listener,
                errorListener
        ) {
            @Override
            public byte[] getBody() {
                return "{}".getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + getToken());
                headers.put("Content-Type", "application/json");
                headers.put("secret-key", Constants.SECRET_KEY);
                return headers;
            }
        };

        requestQueue.add(request);
    }

    public void sendRegistration(String employeeCode, String deviceName, String uniqueId, String remarks,
                                 Response.Listener<String> listener, Response.ErrorListener errorListener) {

        StringRequest request = new StringRequest(Request.Method.POST, Constants.REGISTRATION_URL,
                listener,
                errorListener
        ) {
            @Override
            public byte[] getBody() {
                JSONObject json = new JSONObject();
                try {
                    json.put("EmployeeCode", employeeCode);
                    json.put("DeviceName", deviceName);
                    json.put("DeviceUniqueId", uniqueId);
                    json.put("Remarks", remarks);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return json.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + getToken());
                headers.put("Content-Type", "application/json");
                headers.put("secret-key", Constants.SECRET_KEY);
                return headers;
            }
        };

        requestQueue.add(request);
    }

    public String getToken() {
        return sharedPreferences.getString(Constants.KEY_TOKEN, "");
    }

    public void clearTokens() {
        sharedPreferences.edit().remove(Constants.KEY_TOKEN).apply();
    }

    public RequestQueue getRequestQueue() {
        return requestQueue;
    }

    public void addToRequestQueue(JsonObjectRequest request) {
        requestQueue.add(request);
    }


}
