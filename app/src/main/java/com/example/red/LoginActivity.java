package com.example.red;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent; // For password toggle
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar; // For progress indicator
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

// Import for device ID
import android.provider.Settings;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmployeeCode, edtPassword;
    private Button btnLogin;
    private ImageView ivTogglePassword;
    private ProgressBar progressBar;

    private View btnRegisterDevice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtEmployeeCode = findViewById(R.id.edtEmployeeCode);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegisterDevice = findViewById(R.id.btnRegisterDevice); // TextView

        ivTogglePassword = findViewById(R.id.ivTogglePassword);
        progressBar = findViewById(R.id.progressBar);

        progressBar.setVisibility(View.GONE);

        btnRegisterDevice.setOnClickListener(v -> {
            Toast.makeText(LoginActivity.this, "Clicked!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, DeviceRegistrationActivity.class);
            startActivity(intent);
        });
        btnRegisterDevice.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, DeviceRegistrationActivity.class);
            startActivity(intent);
        });

        btnLogin.setOnClickListener(v -> attemptLogin());






        ivTogglePassword.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                edtPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ivTogglePassword.setImageResource(R.drawable.ic_visibility_on);
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                edtPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ivTogglePassword.setImageResource(R.drawable.ic_visibility_off);
            }
            edtPassword.setSelection(edtPassword.getText().length());
            return true;
        });
    }



    private void attemptLogin() {
        String employeeCode = edtEmployeeCode.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (TextUtils.isEmpty(employeeCode)) {
            edtEmployeeCode.setError("Employee Code is required.");
            edtEmployeeCode.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            edtPassword.setError("Password is required.");
            edtPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("EmployeeCode", employeeCode);
            requestBody.put("Password", password);
            requestBody.put("AppId", Constants.APP_ID);
            requestBody.put("DeviceId", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to create request body", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            btnLogin.setEnabled(true);
            return;
        }




        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                Constants.LOGIN_URL,
                requestBody,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);
                    try {
                        if (response.optInt("status_Code") == 200) {
                            JSONObject data = response.getJSONObject("data");

                            String token = data.optString("token", "");
                            String name = data.optString("name", "User");
                            String email = data.optString("emailAddress", "");
                            String profilePic = data.optString("profilePicture", "");
                            String userId = data.optString("userLogId", "");

                            SharedPreferences prefs = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE);
                            prefs.edit()
                                    .putString(Constants.KEY_TOKEN, token)
                                    .putString(Constants.KEY_USER_NAME, name)
                                    .putString(Constants.KEY_PROFILE_PIC, profilePic)
                                    .putString(Constants.KEY_USER_ID, userId)
                                    .apply();

                            Toast.makeText(LoginActivity.this, "Welcome, " + name, Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(LoginActivity.this, HomeActivity.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                            finish();
                        } else {
                            String message = response.optString("message", "Login failed.");
                            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(LoginActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);
                    String errorMessage = "Login failed.";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            String body = new String(error.networkResponse.data, "UTF-8");
                            JSONObject errObj = new JSONObject(body);
                            errorMessage = errObj.optString("message", errorMessage);
                        } catch (Exception ignored) {}
                    }
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    Log.e("LOGIN_ERROR", "Volley Error: " + error.getMessage(), error);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("secret-key", Constants.SECRET_KEY);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };

        btnRegisterDevice.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, DeviceRegistrationActivity.class);
            startActivity(intent);
        });


        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        ApiRequest.getInstance(this).addToRequestQueue(request);
    }

    private String getAndroidDeviceId() {
        return Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
    }



}