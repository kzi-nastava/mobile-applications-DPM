package com.example.dpm.Activity;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.dpm.Fragment.DriveHistoryFragment;
import com.example.dpm.Fragment.HomePageFragment;
import com.example.dpm.Fragment.ProfileFragment;
import com.example.dpm.Model.UserRole;
import com.example.dpm.R;
import com.example.dpm.Session.UserSession;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    MaterialToolbar toolbar;
    NavigationView navigationView;

    UserSession session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        session = UserSession.getInstance();
        boolean loggedIn = session.isLoggedIn();

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
                if(!loggedIn) {
                    Toast.makeText(
                            this,
                            "You need to login first!",
                            Toast.LENGTH_SHORT
                    ).show();
                }
                else {
                    selectedFragment = new ProfileFragment();
                }
            } else if (id == R.id.bottom_bar_notification) {
                if(!loggedIn) {
                    Toast.makeText(
                            this,
                            "You need to login first!",
                            Toast.LENGTH_SHORT
                    ).show();
                }
                else {
                    //selectedFragment = new ProfileFragment();
                }
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
            if (item.getItemId() == R.id.nav_logout) {

                FirebaseAuth.getInstance().signOut();
                UserSession.getInstance().clear();

                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new HomePageFragment()).commit();
        }

        updateDrawerMenu();

    }

    private void updateDrawerMenu() {

        session = UserSession.getInstance();
        boolean loggedIn = session.isLoggedIn();

        Menu menu = navigationView.getMenu();

        menu.findItem(R.id.nav_login).setVisible(!loggedIn);
        menu.findItem(R.id.nav_register).setVisible(!loggedIn);
        menu.findItem(R.id.nav_logout).setVisible(loggedIn);

        if (loggedIn && session.getUser() != null && session.getUser().getRole() == UserRole.DRIVER){
            menu.findItem(R.id.nav_history).setVisible(true);
        } else {
            menu.findItem(R.id.nav_history).setVisible(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDrawerMenu();
    }

}
