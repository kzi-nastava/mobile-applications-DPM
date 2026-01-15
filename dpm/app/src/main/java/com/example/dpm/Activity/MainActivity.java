package com.example.dpm.Activity;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.dpm.Fragment.DriveHistoryFragment;
import com.example.dpm.Fragment.HomePageFragment;
import com.example.dpm.Fragment.ProfileFragment;
import com.example.dpm.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    MaterialToolbar toolbar;
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawerLayout);
        toolbar = findViewById(R.id.materialToolbar);
        navigationView = findViewById(R.id.navigationView);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        toolbar.setNavigationOnClickListener(v ->
                drawerLayout.openDrawer(GravityCompat.START)
        );


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int id = item.getItemId();

            if (id == R.id.bottom_bar_home) {
                selectedFragment = new HomePageFragment();
            } else if (id == R.id.bottom_bar_profile) {
                selectedFragment = new ProfileFragment();
            } else if (id == R.id.bottom_bar_notification) {

            } else if (id == R.id.bottom_bar_logout) {

            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, selectedFragment).commit();
                return true;
            }

            return false;
        });

        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_login) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
            if (item.getItemId() == R.id.nav_register) {
                startActivity(new Intent(this, RegisterActivity.class));
            }
            if (item.getItemId() == R.id.nav_history) {
                getSupportFragmentManager().beginTransaction().replace( R.id.frameLayout, new DriveHistoryFragment()).commit();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new HomePageFragment()).commit();
        }

    }

}
