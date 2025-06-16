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
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
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
import com.android.volley.toolbox.Volley;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
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
        setContentView(R.layout.activity_home);

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

        dashboardItems = new ArrayList<>();
        dashboardAdapter = new DashboardAdapter(this, dashboardItems);
        recyclerView.setAdapter(dashboardAdapter);
        dashboardAdapter.setClickListener(this); // Set HomeActivity as the click listener

        loadUserProfile();
        loadStaticDashboardItems(); // Load the specific four items for the cube layout
        loadMenuFromApi();
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
                    .into(navProfileImage);
        }
    }

    // This method now uses the DashboardItem constructor with iconResId
    private void loadStaticDashboardItems() {
        dashboardItems.clear();

        // Using iconResId for the 2x2 grid items with ripple effect
        dashboardItems.add(new DashboardItem("New", R.drawable.ic_baseline_person_add_24, 1));
        dashboardItems.add(new DashboardItem("Draft", R.drawable.ic_baseline_edit_note_24, 2));
        dashboardItems.add(new DashboardItem("Pending FIs", R.drawable.ic_baseline_file_copy_24, 3));
        dashboardItems.add(new DashboardItem("Rejected", R.drawable.ic_baseline_block_24, 4));

        // Example of adding an item that *might* come from an API and use a URL
        // If you had other dashboard items fetched from an API, you'd add them here like this:
        // dashboardItems.add(new DashboardItem("Some API Item", "http://example.com/icon.png", 5));


        dashboardAdapter.notifyDataSetChanged();
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
                        Menu menu = navigationView.getMenu();
                        menu.clear();

                        String[] orderedTitles = {
                                "Home", "Identity Card", "Verification History", "Assign/Transfer FIs",
                                "My Payout", "Change Password", "Logout"
                        };

                        Map<String, JSONObject> tempMap = new HashMap<>();
                        for (int i = 0; i < menuItems.length(); i++) {
                            JSONObject item = menuItems.getJSONObject(i);
                            tempMap.put(item.getString("name"), item);
                        }

                        for (String title : orderedTitles) {
                            JSONObject item = tempMap.get(title);
                            if (item != null) {
                                int menuId = item.getInt("menuId");
                                menuDataMap.put(menuId, item);

                                MenuItem menuItem = menu.add(Menu.NONE, menuId, Menu.NONE, title);
                                if (title.equals("Logout")) {
                                    logoutMenuItemId = menuId;
                                }

                                String iconUrl = item.optString("icon", "");
                                if (!iconUrl.isEmpty()) {
                                    // Picasso.get().load(iconUrl).into(new MenuIconTarget(this, menuItem));
                                }
                            }
                        }
                    } catch (JSONException e) {
                        handleApiError(e, "Menu");
                    } finally {
                        dismissProgressDialog();
                    }
                },
                error -> {
                    handleApiError(error, "Menu");
                    dismissProgressDialog();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return getAuthHeaders();
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

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
                Toast.makeText(this, title + " clicked", Toast.LENGTH_SHORT).show();
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    // Implementation of DashboardAdapter.ItemClickListener
    @Override
    public void onItemClick(View view, DashboardItem item) {
        // This method is called when a dashboard item is clicked
        Toast.makeText(this, "Dashboard item clicked: " + item.getTitle(), Toast.LENGTH_SHORT).show();

        // Example: If certain dashboard items should open a WebView, handle it here
        // based on their menuId. This assumes your static items might also have
        // associated WebView URLs (e.g., Identity Card, Verification History).
        JSONObject menuItemData = menuDataMap.get(item.getMenuId());
        if (menuItemData != null) {
            boolean isWebView = menuItemData.optBoolean("isWebView", false);
            if (isWebView) {
                fetchWebViewUrlAndOpen(String.valueOf(item.getMenuId()), item.getTitle());
            } else {
                // Handle non-WebView dashboard item clicks if necessary
                Toast.makeText(this, "Non-WebView dashboard item: " + item.getTitle(), Toast.LENGTH_SHORT).show();
            }
        } else {
            // Handle cases where the dashboard item's menuId is not found in menuDataMap
            Toast.makeText(this, "No specific action defined for " + item.getTitle(), Toast.LENGTH_SHORT).show();
        }
    }


    private void fetchWebViewUrlAndOpen(String itemId, String title) {
        showProgressDialog("Loading " + title + "...");
        String url = Constants.BASE_URL + "api/MobileAppMenu/GetWebViewUrl?MenuId=1" + itemId;

        Log.d("WebViewDebug", "Requesting actual WebView URL from API: " + url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        Log.d("WebViewDebug", "API Response for GetWebViewUrl: " + response.toString());

                        JSONObject dataObj = response.optJSONObject("data");
                        if (dataObj == null) {
                            Toast.makeText(this, "No URL found in API response for " + title, Toast.LENGTH_SHORT).show();
                            Log.e("WebViewDebug", "Data object is null in GetWebViewUrl response.");
                            return;
                        }

                        String finalUrl = dataObj.optString("url", "").trim();
                        Log.d("WebViewDebug", "Extracted final URL from API: '" + finalUrl + "'");

                        if (finalUrl.isEmpty()) {
                            Toast.makeText(this, "Received empty URL from API for " + title, Toast.LENGTH_SHORT).show();
                            Log.e("WebViewDebug", "Received empty URL after extraction.");
                            return;
                        }

                        try {
                            URI uri = new URI(finalUrl);
                            finalUrl = uri.toString();
                            Log.d("WebViewDebug", "Sanitized final URL: '" + finalUrl + "'");
                        } catch (URISyntaxException e) {
                            Toast.makeText(this, "Invalid URL format received from API for " + title, Toast.LENGTH_SHORT).show();
                            Log.e("WEBVIEW", "Invalid URL syntax received from API: " + finalUrl, e);
                            return;
                        }

                        if (Patterns.WEB_URL.matcher(finalUrl).matches()) {
                            Intent intent = new Intent(this, WebViewActivity.class);
                            intent.putExtra("url", finalUrl);
                            intent.putExtra("title", title);
                            startActivity(intent);
                        } else {
                            Toast.makeText(this, "Malformed URL after validation: " + finalUrl, Toast.LENGTH_SHORT).show();
                            Log.e("WEBVIEW", "Malformed URL after validation: " + finalUrl);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Failed to open WebView for " + title + " (response parsing error)", Toast.LENGTH_SHORT).show();
                        Log.e("WebViewDebug", "Error parsing GetWebViewUrl response: " + e.getMessage(), e);
                    } finally {
                        dismissProgressDialog();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Failed to fetch URL from server for " + title + " (network error)", Toast.LENGTH_SHORT).show();
                    Log.e("WebViewDebug", "Volley error fetching GetWebViewUrl: " + error.getMessage(), error);
                    dismissProgressDialog();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + getToken());
                headers.put("secret-key", Constants.SECRET_KEY);
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
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
        // startActivity(new Intent(this, LoginActivity.class));
        // finish();
    }

    private void handleApiError(Exception error, String source) {
        Toast.makeText(this, source + " error", Toast.LENGTH_SHORT).show();
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
        dismissProgressDialog();
        super.onDestroy();
    }


}