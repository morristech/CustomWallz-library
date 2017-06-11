package com.dm.wallpaper.board.fragments;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.utils.Extras;

public class WallpaperPagerFragment extends Fragment {

    private ViewPager viewPager;
    private WallpaperPagerAdaptor adapter;
    private String [] pagerTitles;

    public WallpaperPagerFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_wallpaper_pager, container, false);
        TabLayout tabLayout = (TabLayout) v.findViewById(R.id.tab_layout);
        pagerTitles = getResources().getStringArray(R.array.wallpaper_fragment_pager_tab_titles);
        adapter = new WallpaperPagerAdaptor(getChildFragmentManager());

        viewPager = (ViewPager) v.findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setupWithViewPager(viewPager);

        return v;
    }

    public Fragment getCurrentPagerFragment() {
        return adapter.getFragment(viewPager.getCurrentItem());
    }

    private class WallpaperPagerAdaptor extends FragmentPagerAdapter {

        SparseArray<Fragment> fragments = new SparseArray<>();

        WallpaperPagerAdaptor(FragmentManager fm) {
            super(fm);
            pagerTitles = getContext().getResources().getStringArray(R.array.wallpaper_fragment_pager_tab_titles);
        }

        @Override
        public int getCount() {
            return pagerTitles.length;
        }

        @Override
        public Fragment getItem(int position) {
            Bundle bundle = new Bundle();
            if (position == 0) {
                // The first fragment contains only non Animal wallpapers
                bundle.putString(Extras.INCLUDE_FILTER_TAGS, "art");
            } else if (position == 1) {
                bundle.putString(Extras.EXCLUDE_FILTER_TAGS, "art");
            }

            WallpapersFragment fragment = new WallpapersFragment();
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return pagerTitles[position];
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            fragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            fragments.remove(position);
            super.destroyItem(container, position, object);
        }

        private Fragment getFragment(int positon) {
            return fragments.get(positon);
        }
    }
}
