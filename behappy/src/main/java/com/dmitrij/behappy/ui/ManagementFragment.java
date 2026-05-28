package com.dmitrij.behappy.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.dmitrij.behappy.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class ManagementFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab_container, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        TabLayout tabs = view.findViewById(R.id.tab_layout);
        ViewPager2 pager = view.findViewById(R.id.view_pager);

        pager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int pos) {
                switch (pos) {
                    case 0: return new ServicesFragment();
                    case 1: return new VmFragment();
                    case 2: return new AppsFragment();
                    case 3: return new NetworkFragment();
                    case 4: return new AuditFragment();
                    default: return new ServicesFragment();
                }
            }

            @Override
            public int getItemCount() {
                return 5;
            }
        });

        new TabLayoutMediator(tabs, pager, (tab, pos) -> {
            switch (pos) {
                case 0: 
                    tab.setText(R.string.nav_services);
                    tab.setIcon(R.drawable.settings); 
                    break;
                case 1: 
                    tab.setText(R.string.nav_virtualization);
                    tab.setIcon(R.drawable.virtual_machine);
                    break;
                case 2: 
                    tab.setText(R.string.nav_apps);
                    tab.setIcon(R.drawable.app);
                    break;
                case 3: 
                    tab.setText(R.string.nav_network);
                    tab.setIcon(R.drawable.wi_fi);
                    break;
                case 4: 
                    tab.setText(R.string.nav_audit);
                    tab.setIcon(R.drawable.terminal);
                    break;
            }
        }).attach();
    }
}
