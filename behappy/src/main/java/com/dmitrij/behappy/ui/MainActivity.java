package com.dmitrij.behappy.ui;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.dmitrij.behappy.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.color.DynamicColors;

public class MainActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        DynamicColors.applyToActivitiesIfAvailable(this.getApplication());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }

        viewPager = findViewById(R.id.view_pager);
        bottomNav = findViewById(R.id.bottom_navigation);

        com.dmitrij.behappy.widget.WidgetRefreshWorker.enqueue(this);

        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                switch (position) {
                    case 0: return new DashboardFragment();
                    case 1: return new StorageFragment();
                    case 2: return new ManagementFragment();
                    default: return new DashboardFragment();
                }
            }

            @Override
            public int getItemCount() {
                return 3;
            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0: bottomNav.setSelectedItemId(R.id.nav_dashboard); break;
                    case 1: bottomNav.setSelectedItemId(R.id.nav_storage); break;
                    case 2: bottomNav.setSelectedItemId(R.id.nav_management); break;
                }
            }
        });

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
                viewPager.setCurrentItem(0);
                return true;
            } else if (id == R.id.nav_storage) {
                viewPager.setCurrentItem(1);
                return true;
            } else if (id == R.id.nav_management) {
                viewPager.setCurrentItem(2);
                return true;
            }
            return false;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        android.view.MenuItem settingsItem = menu.findItem(R.id.action_settings);
        if (settingsItem != null && settingsItem.getIcon() != null) {
            settingsItem.getIcon().setTint(androidx.core.content.ContextCompat.getColor(this, R.color.tg_blue));
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
            
            bottomNav.setVisibility(View.GONE);
            viewPager.setVisibility(View.GONE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            bottomNav.setVisibility(View.VISIBLE);
            viewPager.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }
}
