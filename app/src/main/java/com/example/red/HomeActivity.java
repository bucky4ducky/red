package com.example.red;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, DashboardAdapter.ItemClickListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ProgressDialog progressDialog;
    private DashboardAdapter dashboardAdapter;
    private ArrayList<DashboardItem> dashboardItems;
    private ImageView navProfileImage;
    private TextView navName;
    private int logoutMenuItemId = -1;

    private Map<Integer, JSONObject> menuDataMap = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isLoggedIn()) {
            Toast.makeText(this, "Session expired or not logged in. Please log in.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_home);

        dashboardItems = new ArrayList<>();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);


        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");

        View headerView = navigationView.getHeaderView(0);
        navProfileImage = headerView.findViewById(R.id.nav_image);
        navName = headerView.findViewById(R.id.nav_name);


        RecyclerView recyclerView = findViewById(R.id.dashboardRecycler);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        dashboardAdapter = new DashboardAdapter(this, dashboardItems);
        recyclerView.setAdapter(dashboardAdapter);
        dashboardAdapter.setClickListener(this);

        loadUserProfile();
        loadMenuFromApi();

        loadDashboardFromApi();

    }
    private boolean isLoggedIn() {
        SharedPreferences prefs = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE);
        String token = prefs.getString("token", "");
        return !TextUtils.isEmpty(token);
    }

    private void showProgressDialog(String message) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
        }
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
    private void loadUserProfile() {
        SharedPreferences prefs = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE);
        String profilePicUrl = prefs.getString("profilePic", "");
        String name = prefs.getString("name", "");

        if (navName != null) {
            navName.setText(name);
        }
        if (!profilePicUrl.isEmpty() && navProfileImage != null) {
            Picasso.get().load(profilePicUrl)
                    .transform(new CircleTransform())
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .into(navProfileImage);
        } else if (navProfileImage != null) {
            navProfileImage.setImageResource(R.drawable.ic_profile_placeholder);
        }
    }
    private void loadMenuFromApi() {
        showProgressDialog("Loading menu...");
        menuDataMap.clear();

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                Constants.MENU_URL,
                null,
                response -> {
                    try {
                        JSONArray menuItems = response.getJSONArray("data");
                        List<JSONObject> sortedItems = new ArrayList<>();

                        for (int i = 0; i < menuItems.length(); i++) {
                            sortedItems.add(menuItems.getJSONObject(i));
                        }

                        Collections.sort(sortedItems, (a, b) -> {
                            try {
                                return Integer.compare(a.getInt("orderNo"), b.getInt("orderNo"));
                            } catch (JSONException e) {
                                return 0;
                            }
                        });

                        Menu menu = navigationView.getMenu();
                        menu.clear();

                        for (JSONObject item : sortedItems) {
                            String title = item.getString("name");
                            int menuId = item.getInt("menuId");
                            String iconUrl = item.optString("icon", "");

                            menuDataMap.put(menuId, item);

                            MenuItem menuItem = menu.add(Menu.NONE, menuId, Menu.NONE, title);

                            if ("Logout".equalsIgnoreCase(title)) {
                                logoutMenuItemId = menuId;
                            }

                            if (!iconUrl.isEmpty()) {
                                Picasso.get()
                                        .load(iconUrl)
                                        .placeholder(R.drawable.ic_profile_placeholder)
                                        .into(new MenuIconTarget(HomeActivity.this, menuItem));
                            } else {
                                menuItem.setIcon(R.drawable.ic_baseline_home_24);
                            }
                        }

                    } catch (JSONException e) {
                        handleApiError(e, "Menu Data Parsing");
                    } finally {
                        dismissProgressDialog();
                    }
                },
                error -> {
                    handleApiError(error, "Menu API Request");
                    dismissProgressDialog();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + getToken());
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

    private void loadDashboardFromApi() {
        showProgressDialog("Loading dashboard...");

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                Constants.BASE_URL + "api/MobileAppMenu/GetDashboard",
                null,
                response -> {
                    dismissProgressDialog();
                    try {
                        JSONObject data = response.optJSONObject("data");
                        if (data != null) {
                            JSONArray dashboardArray = data.optJSONArray("mobileDashboardDetailDto");
                            if (dashboardArray != null) {
                                dashboardItems.clear();
                                for (int i = 0; i < dashboardArray.length(); i++) {
                                    JSONObject item = dashboardArray.getJSONObject(i);
                                    String title = item.optString("buttonCaption");
                                    String iconUrl = item.optString("icon");
                                    int menuId = item.optInt("buttonId");

                                    dashboardItems.add(new DashboardItem(title, iconUrl, menuId));
                                }
                                dashboardAdapter.notifyDataSetChanged();
                            }
                        }
                    } catch (JSONException e) {
                        handleApiError(e, "Dashboard Data Parsing");
                    }
                },
                error -> {
                    dismissProgressDialog();
                    handleApiError(error, "Dashboard API Request");
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return getAuthHeaders();
            }
        };

        ApiRequest.getInstance(this).addToRequestQueue(request);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int clickedMenuId = item.getItemId();
        JSONObject menuItemData = menuDataMap.get(clickedMenuId);

        if (menuItemData != null) {
            boolean isWebView = menuItemData.optBoolean("isWebView", false);
            String title = item.getTitle().toString();

            if (isWebView) {
                fetchWebViewUrlAndOpen(Integer.toString(clickedMenuId), title);
            } else if (clickedMenuId == logoutMenuItemId) {
                logout();
            } else {
                Toast.makeText(this, title + " clicked (ID: " + clickedMenuId + ")", Toast.LENGTH_SHORT).show();
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onItemClick(View view, DashboardItem item) {
        Toast.makeText(this, "Dashboard item clicked: " + item.getTitle() + " (Menu ID: " + item.getMenuId() + ")", Toast.LENGTH_SHORT).show();

        JSONObject menuItemData = menuDataMap.get(item.getMenuId());
        if (menuItemData != null && menuItemData.optBoolean("isWebView", false)) {
            fetchWebViewUrlAndOpen(String.valueOf(item.getMenuId()), item.getTitle());
        } else {
            Toast.makeText(this, "Action for dashboard item '" + item.getTitle() + "' not defined as WebView or not found in menu data.", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchWebViewUrlAndOpen(String itemId, String title) {
        showProgressDialog("Loading " + title + "...");
        String urlToFetch = Constants.BASE_URL + "api/MobileAppMenu/GetWebViewUrl?MenuId="+itemId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, urlToFetch, null,
                response -> {
                    try {
                        JSONObject dataObj = response.optJSONObject("data");
                        if (dataObj == null) {
                            Toast.makeText(this, "No URL data found in API response for " + title, Toast.LENGTH_LONG).show();
                            return;
                        }

                        String finalUrl = dataObj.optString("url", "").trim();
                        if (finalUrl.isEmpty()) {
                            Toast.makeText(this, "Received empty URL from API for " + title, Toast.LENGTH_LONG).show();
                            return;
                        }

                        if (finalUrl.startsWith("/")) {
                            finalUrl = "https://mayaglobalservices.in" ;
                        }

                        try {
                            URI uri = new URI(finalUrl);
                            finalUrl = uri.toASCIIString();
                        } catch (URISyntaxException e) {
                            Toast.makeText(this, "Invalid URL format received from API for " + title, Toast.LENGTH_LONG).show();
                            return;
                        }

                        if (Patterns.WEB_URL.matcher(finalUrl).matches()) {
                            Intent intent = new Intent(this, WebViewActivity.class);
                            intent.putExtra("url", finalUrl);
                            intent.putExtra("title", title);
                            startActivity(intent);
                        } else {
                            Toast.makeText(this, "Malformed URL after validation: " + finalUrl, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Failed to open WebView for " + title + " (response processing error)", Toast.LENGTH_LONG).show();
                    } finally {
                        dismissProgressDialog();
                    }
                },
                error -> {
                    Toast.makeText(this, "Failed to fetch URL from server for " + title + " (network error)", Toast.LENGTH_LONG).show();
                    dismissProgressDialog();
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + getToken());
                headers.put("secret-key", Constants.SECRET_KEY);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        ApiRequest.getInstance(this).addToRequestQueue(request);
    }


    private Map<String, String> getAuthHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + getToken());
        headers.put("secret-key", Constants.SECRET_KEY);
        headers.put("Content-Type", "application/json");
        return headers;
    }

    private String getToken() {
        SharedPreferences prefs = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE);
        return prefs.getString("token", "");
    }

    private void logout() {
        Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();
        getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE).edit().clear().apply();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void handleApiError(Exception error, String source) {
        String errorMessage = error != null && error.getMessage() != null ? error.getMessage() : "An error occurred";
        Toast.makeText(this, source + " failed: " + errorMessage, Toast.LENGTH_LONG).show();
        Log.e("API_ERROR", source + " failed", error);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (ApiRequest.getInstance(this).getRequestQueue() != null) {
            ApiRequest.getInstance(this).getRequestQueue().cancelAll(this);
        }
        dismissProgressDialog();
        super.onDestroy();
    }
}