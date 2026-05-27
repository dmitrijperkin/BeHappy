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

public class StorageFragment extends Fragment {

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
                    case 0: return new PoolsFragment();
                    case 1: return new SmbFragment();
                    default: return new DisksFragment();
                }
            }

            @Override
            public int getItemCount() {
                return 3;
            }
        });

        new TabLayoutMediator(tabs, pager, (tab, pos) -> {
            switch (pos) {
                case 0: 
                    tab.setText(R.string.nav_storage);
                    tab.setIcon(R.drawable.harddisk);
                    break;
                case 1: 
                    tab.setText(R.string.nav_smb);
                    tab.setIcon(R.drawable.info);
                    break;
                case 2: 
                    tab.setText(R.string.nav_disks);
                    tab.setIcon(R.drawable.harddisk);
                    break;
            }
        }).attach();
    }
}
