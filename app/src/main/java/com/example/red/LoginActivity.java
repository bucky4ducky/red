
package com.example.red;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private EditText edtEmployeeCode, edtPassword;
    private boolean isPasswordVisible = false;

    private ProgressDialog progressDialog;
    private ImageView ivTogglePassword;


    private ImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        profileImage = findViewById(R.id.profile_image);
        edtEmployeeCode = findViewById(R.id.edtEmployeeCode);
        edtPassword = findViewById(R.id.edtPassword);

        ivTogglePassword = findViewById(R.id.ivTogglePassword);

        ivTogglePassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ivTogglePassword.setImageResource(R.drawable.ic_visibility_off);
            } else {
                edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ivTogglePassword.setImageResource(R.drawable.ic_visibility);
            }
            edtPassword.setSelection(edtPassword.getText().length());
            isPasswordVisible = !isPasswordVisible;
        });

        findViewById(R.id.btnLogin).setOnClickListener(v -> attemptLogin());

        // Auto-login
        if (getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE)
                .getString("token", null) != null) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }
    }

    private void attemptLogin() {
        String empCode = edtEmployeeCode.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (empCode.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter credentials!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");
        progressDialog.show();

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("EmployeeCode", empCode);
            jsonBody.put("Password", password);
            jsonBody.put("AppId", Constants.APP_ID);
            jsonBody.put("DeviceId", "android_device");

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    Constants.LOGIN_URL,
                    jsonBody,
                    response -> handleLoginResponse(response),
                    error -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show();
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("secret-key", Constants.SECRET_KEY);
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            ApiRequest.getInstance(this).addToRequestQueue(request);
        } catch (Exception e) {
            progressDialog.dismiss();
            e.printStackTrace();
        }
    }

    private void handleLoginResponse(JSONObject response) {
        progressDialog.dismiss();
        try {
            if (response.getInt("status_Code") == 200) {
                JSONObject data = response.getJSONObject("data");

                SharedPreferences.Editor editor = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE).edit();
                editor.putString("token", data.getString("token"));
                editor.putString("name", data.getString("name"));
                editor.putString("profilePic", data.getString("profilePicture"));
                editor.apply();

                startActivity(new Intent(this, HomeActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Login failed: " + response.getString("message"), Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            Toast.makeText(this, "Login error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

}
