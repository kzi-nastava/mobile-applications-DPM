package com.example.dpm.Activity;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.dpm.Fragment.AdminChangeDataRequestsFragment;
import com.example.dpm.Fragment.AdminChatListFragment;
import com.example.dpm.Fragment.AdminHistoryFragment;
import com.example.dpm.Fragment.AdminUsersFragment;
import com.example.dpm.Fragment.DriveHistoryFragment;
import com.example.dpm.Fragment.DriverRidesFragment;
import com.example.dpm.Fragment.HomePageFragment;
import com.example.dpm.Fragment.NotificationsFragment;
import com.example.dpm.Fragment.PassengerRidesFragment;
import com.example.dpm.Fragment.PricingFragment;
import com.example.dpm.Fragment.ProfileFragment;
import com.example.dpm.Fragment.RatingDialogFragment;
import com.example.dpm.Fragment.RideReportFragment;
import com.example.dpm.Fragment.RideStateViewFragment;
import com.example.dpm.Fragment.RideTrackingFragment;
import com.example.dpm.Fragment.SupportChatFragment;
import com.example.dpm.Model.UserRole;
import com.example.dpm.R;
import com.example.dpm.Repository.NotificationRepository;
import com.example.dpm.Repository.RatingRepository;
import com.example.dpm.Repository.UserRepository;
import com.example.dpm.Session.UserSession;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.example.dpm.Fragment.PassengerHistoryFragment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class MainActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    MaterialToolbar toolbar;
    NavigationView navigationView;
    ListenerRegistration unreadListener;
    NotificationRepository notificationRepository;
    private boolean pendingChecked = false;
    UserSession session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        session = UserSession.getInstance();
        boolean loggedIn;
        if(session == null)
            loggedIn = false;
        else {
            loggedIn = true;
        }

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

        if (loggedIn && session.getUser() != null) {
            NotificationRepository notificationRepository = new NotificationRepository();

            unreadListener = notificationRepository.listenUnreadCount(
                    session.getUser().getId(),
                    (snapshots, e) -> {
                        if (e != null || snapshots == null) return;

                        int count = snapshots.size();

                        com.google.android.material.badge.BadgeDrawable badge =
                                bottomNavigationView.getOrCreateBadge(R.id.bottom_bar_notification);

                        if (count > 0) {
                            badge.setVisible(true);
                            badge.setNumber(count);
                        } else {
                            badge.clearNumber();
                            badge.setVisible(false);
                        }
                    }
            );
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int id = item.getItemId();

            if (id == R.id.bottom_bar_home) {
                selectedFragment = new HomePageFragment();
            } else if (id == R.id.bottom_bar_profile) {
                if(!loggedIn) {
                    Toast.makeText(this, "You need to login first!", Toast.LENGTH_SHORT).show();
                }
                else {
                    selectedFragment = new ProfileFragment();
                }
            } else if (id == R.id.bottom_bar_notification) {
                if(!loggedIn) {
                    Toast.makeText(this, "You need to login first!", Toast.LENGTH_SHORT).show();
                }
                else {
                    selectedFragment = new NotificationsFragment();

//                    RatingDialogFragment dialog = RatingDialogFragment.newInstance("TEST_RIDE_ID_123");
//                    dialog.show(getSupportFragmentManager(), "rating_dialog");
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
                if (session.getUser() != null && session.getUser().getRole() == UserRole.ADMIN) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.frameLayout, new AdminHistoryFragment())
                            .commit();
                } else if (session.getUser() != null && session.getUser().getRole() == UserRole.DRIVER) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.frameLayout, new DriveHistoryFragment())
                            .commit();
                } else {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.frameLayout, new PassengerHistoryFragment())
                            .commit();
                }
            }
            if (item.getItemId() == R.id.nav_pricing) {
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new PricingFragment()).commit();
            }
            if (item.getItemId() == R.id.nav_view_rides) {
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new RideStateViewFragment()).commit();
            }
            if (item.getItemId() == R.id.nav_support_chat) {
               getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new SupportChatFragment()).commit();
            }
            if (item.getItemId() == R.id.nav_support_inbox) {
               getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new AdminChatListFragment()).commit();
            }
            if (item.getItemId() == R.id.nav_my_rides) {
                if (session.getUser() != null && session.getUser().getRole() == UserRole.DRIVER) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.frameLayout, new DriverRidesFragment())
                            .commit();
                } else if (session.getUser() != null && session.getUser().getRole() == UserRole.PASSENGER) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.frameLayout, new PassengerRidesFragment())
                            .commit();
                }
            }
            if (item.getItemId() == R.id.nav_logout) {

                FirebaseAuth.getInstance().signOut();
                UserSession.getInstance().clear();

                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
            if(item.getItemId() == R.id.nav_blocking_users) {
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new AdminUsersFragment()).commit();
            }
            if(item.getItemId() == R.id.nav_change_data_requests) {
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new AdminChangeDataRequestsFragment()).commit();
            }
            if(item.getItemId() == R.id.nav_report_generation) {
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new RideReportFragment()).commit();
            }
            if(item.getItemId() == R.id.nav_register_new_driver) {
                startActivity(new Intent(this, RegisterDriverActivity.class));
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new HomePageFragment()).commit();
        }
        checkPendingRatingIfPassenger();

        handleDeepLink(getIntent());
        updateDrawerMenu();

    }

    private void updateDrawerMenu() {

        session = UserSession.getInstance();
        boolean loggedIn = session.isLoggedIn();

        Menu menu = navigationView.getMenu();

        menu.findItem(R.id.nav_login).setVisible(!loggedIn);
        menu.findItem(R.id.nav_register).setVisible(!loggedIn);
        menu.findItem(R.id.nav_blocking_users).setVisible(false);
        menu.findItem(R.id.nav_change_data_requests).setVisible(false);
        menu.findItem(R.id.nav_report_generation).setVisible(loggedIn);
        menu.findItem(R.id.nav_pricing).setVisible(false);
        menu.findItem(R.id.nav_view_rides).setVisible(false);
        menu.findItem(R.id.nav_support_chat).setVisible(false);
        menu.findItem(R.id.nav_support_inbox).setVisible(false);
        menu.findItem(R.id.nav_my_rides).setVisible(false);
        menu.findItem(R.id.nav_register_new_driver).setVisible(false);
        menu.findItem(R.id.nav_logout).setVisible(loggedIn);

        if (loggedIn && session.getUser() != null &&
                (session.getUser().getRole() == UserRole.DRIVER
                        || session.getUser().getRole() == UserRole.ADMIN
                        || session.getUser().getRole() == UserRole.PASSENGER)) {
            menu.findItem(R.id.nav_history).setVisible(true);
        } else {
            menu.findItem(R.id.nav_history).setVisible(false);
        }
        if (loggedIn && session.getUser() != null && session.getUser().getRole() == UserRole.DRIVER){
            menu.findItem(R.id.nav_my_rides).setVisible(true);
        }
        if (loggedIn && session.getUser() != null) {
            if (session.getUser().getRole() == UserRole.ADMIN) {
                menu.findItem(R.id.nav_blocking_users).setVisible(true);
                menu.findItem(R.id.nav_pricing).setVisible(true);
                menu.findItem(R.id.nav_view_rides).setVisible(true);
                menu.findItem(R.id.nav_support_inbox).setVisible(true);
                menu.findItem(R.id.nav_register_new_driver).setVisible(true);
                menu.findItem(R.id.nav_change_data_requests).setVisible(true);
            } else {
                menu.findItem(R.id.nav_support_chat).setVisible(true);
            }
        }

        if (loggedIn && session.getUser() != null &&
                (session.getUser().getRole() == UserRole.DRIVER || session.getUser().getRole() == UserRole.PASSENGER)) {
            menu.findItem(R.id.nav_my_rides).setVisible(true);
        }

    }

    private void handleDeepLink(Intent intent) {
        Uri data = intent.getData();
        if (data == null) return;

        if ("ride".equals(data.getHost())) {
            String rideId = data.getQueryParameter("rideId");
            if (rideId != null && !rideId.isEmpty()) {
                RideTrackingFragment frag = RideTrackingFragment.newInstance(rideId);
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, frag).addToBackStack(null).commit();
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleDeepLink(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDrawerMenu();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unreadListener != null) {
            unreadListener.remove();
            unreadListener = null;
        }
    }

    private void checkPendingRatingIfPassenger() {

        if (pendingChecked) return;
        pendingChecked = true;

        UserSession session = UserSession.getInstance();
        if (!session.isLoggedIn() || session.getUser() == null) return;

        if (session.getUser().getRole() != UserRole.PASSENGER) return;

        String uid = session.getUser().getId();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    String rideId = doc.getString("pendingRatingRideId");
                    Long until = doc.getLong("pendingRatingUntil");

                    if (rideId == null || rideId.isEmpty() || until == null) return;

                    long now = System.currentTimeMillis();
                    if (now > until) return;


                    RatingRepository ratingRepo = new RatingRepository();
                    ratingRepo.getRatingsByRideId(rideId, ratings -> {

                        boolean alreadyRated = ratings != null && !ratings.isEmpty();
                        if (alreadyRated) {

                            new UserRepository().clearPendingRating(uid);
                            return;
                        }

                        RatingDialogFragment dialog = RatingDialogFragment.newInstance(rideId);
                        dialog.show(getSupportFragmentManager(), "rating_dialog");
                    });

                });
    }

}
