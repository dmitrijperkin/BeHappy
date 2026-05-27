package com.dmitrij.behappy.ui;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.Manifest;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.dmitrij.behappy.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.color.DynamicColors;

public class MainActivity extends BaseActivity {
    private ViewPager2 pager;
    private BottomNavigationView nav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }

        pager = findViewById(R.id.view_pager);
        nav = findViewById(R.id.bottom_navigation);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        com.dmitrij.behappy.widget.WidgetRefreshWorker.enqueue(this);

        pager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int i) {
                switch (i) {
                    case 0: return new DashboardFragment();
                    case 1: return new StorageFragment();
                    case 2: return new ManagementFragment();
                    case 3: return new SshFragment();
                    default: return new DashboardFragment();
                }
            }

            @Override
            public int getItemCount() {
                return 4;
            }
        });

        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int i) {
                switch (i) {
                    case 0: nav.setSelectedItemId(R.id.nav_dashboard); break;
                    case 1: nav.setSelectedItemId(R.id.nav_storage); break;
                    case 2: nav.setSelectedItemId(R.id.nav_management); break;
                    case 3: nav.setSelectedItemId(R.id.nav_ssh); break;
                }
            }
        });

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
                pager.setCurrentItem(0);
                return true;
            } else if (id == R.id.nav_storage) {
                pager.setCurrentItem(1);
                return true;
            } else if (id == R.id.nav_management) {
                pager.setCurrentItem(2);
                return true;
            } else if (id == R.id.nav_ssh) {
                pager.setCurrentItem(3);
                return true;
            }
            return false;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        android.view.MenuItem settings = menu.findItem(R.id.action_settings);
        if (settings != null && settings.getIcon() != null) {
            settings.getIcon().setTint(androidx.core.content.ContextCompat.getColor(this, R.color.tg_blue));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, new SettingsFragment(), "settings")
                    .addToBackStack(null)
                    .commit();
            nav.setVisibility(View.GONE);
            pager.setVisibility(View.GONE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            nav.setVisibility(View.VISIBLE);
            pager.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }
}
