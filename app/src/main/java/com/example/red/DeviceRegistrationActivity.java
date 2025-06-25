package com.example.red;

import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DeviceRegistrationActivity extends AppCompatActivity {

    private EditText edtEmployeeCode, edtRemarks;
    private Button btnSubmit;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_registration);

        edtEmployeeCode = findViewById(R.id.edtEmployeeCode);
        edtRemarks = findViewById(R.id.edtRemarks);
        btnSubmit = findViewById(R.id.btnSubmit);


        btnSubmit.setOnClickListener(v -> {
            String employeeCode = edtEmployeeCode.getText().toString().trim();
            String remarks = edtRemarks.getText().toString().trim();
            String deviceName = Build.MODEL;
            String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);


            if (employeeCode.isEmpty() || remarks.isEmpty()) {
                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            sendRegistrationRequest(employeeCode, deviceName, deviceId, remarks);
        });
    }


    private void sendRegistrationRequest(String employeeCode, String deviceName, String deviceId, String remarks) {
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("EmployeeCode", employeeCode);
            requestBody.put("DeviceName", deviceName);
            requestBody.put("DeviceUniqueId", deviceId);
            requestBody.put("Remarks", remarks);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to create request", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                Constants.REGISTRATION_URL,
                requestBody,
                response -> {
                    int code = response.optInt("status_Code", 0);
                    String message = response.optString("message", "Unknown response");

                    if (code == 200) {
                        Toast.makeText(this, "Registered Successfully", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "Network error occurred", Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
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

        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        ApiRequest.getInstance(this).addToRequestQueue(request);
    }
}
