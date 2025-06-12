package com.example.red;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ProgressDialog progressDialog;
    private DashboardAdapter dashboardAdapter;
    private ArrayList<DashboardItem> dashboardItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setup navigation drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Setup RecyclerView
        RecyclerView recyclerView = findViewById(R.id.dashboardRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        dashboardAdapter = new DashboardAdapter(this, dashboardItems);
        recyclerView.setAdapter(dashboardAdapter);

        // Load data
        loadDashboard();
        loadMenuFromApi();
    }

    private void loadDashboard() {
        progressDialog = ProgressDialog.show(this, "", "Loading dashboard...");

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                Constants.DASHBOARD_URL,
                null,
                response -> {
                    try {
                        JSONArray items = response.getJSONObject("data")
                                .getJSONArray("mobileDashboardDetailDto");

                        dashboardItems.clear();
                        for (int i = 0; i < items.length(); i++) {
                            JSONObject item = items.getJSONObject(i);
                            dashboardItems.add(new DashboardItem(
                                    item.getString("buttonCaption"),
                                    item.getString("icon")
                            ));
                        }
                        dashboardAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        progressDialog.dismiss();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Dashboard load failed", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + getToken());
                return headers;
            }
        };

        ApiRequest.getInstance(this).addToRequestQueue(request);
    }

    private void loadMenuFromApi() {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                Constants.MENU_URL,
                null,
                response -> {
                    try {
                        JSONArray menuItems = response.getJSONArray("data");
                        Menu menu = navigationView.getMenu();
                        menu.clear();

                        // Define menu order
                        Map<String, Integer> menuOrder = new HashMap<String, Integer>() {{
                            put("Home", 1);
                            put("Identity Card", 2);
                            put("Verification History", 3);
                            put("Assign/Transfer FIs", 4);
                            put("My Payout", 5);
                            put("Change Password", 6);
                            put("Logout", 7);
                        }};

                        List<JSONObject> sortedItems = new ArrayList<>();
                        for (int i = 0; i < menuItems.length(); i++) {
                            sortedItems.add(menuItems.getJSONObject(i));
                        }

                        Collections.sort(sortedItems, (a, b) -> {
                            try {
                                String titleA = a.getString("name");
                                String titleB = b.getString("name");
                                return menuOrder.getOrDefault(titleA, 99) -
                                        menuOrder.getOrDefault(titleB, 99);
                            } catch (JSONException e) {
                                return 0;
                            }
                        });

                        for (JSONObject item : sortedItems) {
                            String title = item.getString("name");
                            int menuId = item.getInt("menuId");
                            String iconUrl = item.getString("icon");

                            MenuItem menuItem = menu.add(Menu.NONE, menuId, Menu.NONE, title);
                            // Optionally set a placeholder icon here:
                            // menuItem.setIcon(R.drawable.ic_placeholder);

                            if (iconUrl != null && !iconUrl.isEmpty()) {
                                Picasso.get()
                                        .load(iconUrl)
                                        .into(new MenuIconTarget(this, menuItem));
                            }
                        }

                        navigationView.setNavigationItemSelectedListener(this);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error loading menu", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Menu load failed", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + getToken());
                headers.put("secret-key", Constants.SECRET_KEY);
                return headers;
            }
        };

        ApiRequest.getInstance(this).addToRequestQueue(request);
    }

    private void logout() {
        getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE).edit().clear().apply();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private String getToken() {
        return getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE)
                .getString("token", "");
    }

    @Override
    protected void onDestroy() {
        ApiRequest.getInstance(this).cancelAll();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        super.onDestroy();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case 1: // Example: Home
                Toast.makeText(this, "Home clicked", Toast.LENGTH_SHORT).show();
                break;
            case 7: // Logout
                logout();
                break;
            default:
                Toast.makeText(this, item.getTitle() + " clicked", Toast.LENGTH_SHORT).show();
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
